package com.msa.core.network.client

import com.msa.core.common.error.AppError
import com.msa.core.common.paging.PageInfo
import com.msa.core.common.result.Meta
import com.msa.core.common.result.Outcome
import com.msa.core.network.cache.CachedEntry
import com.msa.core.network.cache.HttpCacheRepository
import com.msa.core.network.config.CachePolicy
import com.msa.core.network.config.NetHeaders
import com.msa.core.network.config.NetworkConfig
import com.msa.core.network.circuit.CircuitBreaker
import com.msa.core.network.envelope.ApiResponse
import com.msa.core.network.error.ErrorMapper
import com.msa.core.network.util.buildUrl
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import java.io.IOException
import java.util.Locale
import kotlin.math.max

class NetworkClient(
    private val httpClient: HttpClient,
    private val statusMonitor: NetworkStatusMonitor,
    private val errorMapper: ErrorMapper,
    private val cacheRepository: HttpCacheRepository,
    private val circuitBreaker: CircuitBreaker,
    private val config: NetworkConfig
) : RawApi, EnvelopeApi {

    override suspend fun <T> execute(request: NetworkRequest<T>): Outcome<T> =
        executeInternal(
            method = request.method,
            path = request.path,
            query = request.query,
            headers = request.headers,
            body = request.body,
            cacheKeySuffix = "raw",
            parser = request.parser,
            envelopeHandler = null
        )

    override suspend fun <T> execute(request: EnvelopeRequest<T>): Outcome<T> =
        executeInternal(
            method = request.method,
            path = request.path,
            query = request.query,
            headers = request.headers,
            body = request.body,
            cacheKeySuffix = "env",
            parser = null,
            envelopeHandler = request.parser
        )

    private suspend fun <T> executeInternal(
        method: HttpMethod,
        path: String,
        query: Map<String, Any?>,
        headers: Map<String, String>,
        body: Any?,
        cacheKeySuffix: String,
        parser: (suspend (HttpResponse) -> T)?,
        envelopeHandler: (suspend (HttpResponse) -> ApiResponse<T>)?
    ): Outcome<T> {
        if (!circuitBreaker.allow()) {
            return Outcome.Failure(AppError.Network(message = "Circuit open", isConnectivity = true))
        }

        val url = buildUrl(config.baseUrl, path, query)
        val cacheKey = cacheKey(method, cacheKeySuffix)
        val isOffline = !statusMonitor.isOnline()

        if (config.cachePolicy == CachePolicy.OfflineOnly) {
            return readFromCache<T>(cacheKey, url)?.also { circuitBreaker.onSuccess() }
                ?: Outcome.Failure(AppError.Network(message = "Offline cache only", isConnectivity = true))
        }

        if (isOffline) {
            return when (config.cachePolicy) {
                CachePolicy.NoCache -> Outcome.Failure(AppError.Network(message = "Offline", isConnectivity = true))
                CachePolicy.CacheFirst, CachePolicy.NetworkFirst, CachePolicy.OfflineOnly ->
                    readFromCache<T>(cacheKey, url)?.also { circuitBreaker.onSuccess() }
                        ?: Outcome.Failure(AppError.Network(message = "Offline and cache miss", isConnectivity = true))
            }
        }

        if (config.cachePolicy == CachePolicy.CacheFirst) {
            readFromCache<T>(cacheKey, url)?.let {
                circuitBreaker.onSuccess()
                return it
            }
        }

        val startNanos = System.nanoTime()
        return try {
            val etag = cacheRepository.readEtag(cacheKey, url)
            val response = httpClient.request(url) {
                this.method = method
                headers.forEach { (key, value) -> header(key, value) }
                if (etag != null) header(NetHeaders.IF_NONE_MATCH, etag)
                if (body != null) setBody(body)
            }
            handleResponse(cacheKey, url, response, parser, envelopeHandler, startNanos)
        } catch (t: Throwable) {
            circuitBreaker.onFailure()
            if (config.cachePolicy != CachePolicy.NoCache && t is IOException) {
                readFromCache<T>(cacheKey, url)?.let { return it.also { circuitBreaker.onSuccess() } }
            }
            Outcome.Failure(errorMapper.fromException(t, url))
        }
    }

    private suspend fun <T> handleResponse(
        cacheKey: String,
        url: String,
        response: HttpResponse,
        parser: (suspend (HttpResponse) -> T)?,
        envelopeHandler: (suspend (HttpResponse) -> ApiResponse<T>)?,
        startNanos: Long
    ): Outcome<T> {
        val latencyMillis = max((System.nanoTime() - startNanos) / 1_000_000, 0)
        val headersMap = response.headers.entries().associate { (key, value) -> key to value.joinToString(",") }
        val paginationFromHeaders = response.toPageInfo()

        return when {
            response.status.value == 304 -> {
                val cached = cacheRepository.read<T>(cacheKey, url)
                    ?: return Outcome.Failure(
                        AppError.Network(
                            message = "Cache miss on 304",
                            statusCode = 304,
                            isConnectivity = true,
                            endpoint = url
                        )
                    )
                circuitBreaker.onSuccess()
                Outcome.Success(cached.value, cached.meta.copy(statusCode = 304))
            }

            response.status.isSuccess() -> {
                val baseMeta = extractMeta(response, latencyMillis, paginationFromHeaders, headersMap)
                val outcome = when {
                    envelopeHandler != null -> handleEnvelope(cacheKey, url, response, envelopeHandler, baseMeta)
                    parser != null -> handleRaw(cacheKey, url, response, parser, baseMeta)
                    else -> Outcome.Failure(
                        AppError.Unknown(
                            message = "No parser provided",
                            endpoint = url
                        )
                    )
                }
                if (outcome is Outcome.Success) {
                    circuitBreaker.onSuccess()
                } else if (outcome is Outcome.Failure && outcome.error is AppError.Business) {
                    circuitBreaker.onSuccess()
                } else {
                    circuitBreaker.onFailure()
                }
                outcome
            }

            else -> {
                val statusCode = response.status.value
                if (config.cachePolicy == CachePolicy.NetworkFirst && statusCode in config.retry.retryStatusCodes) {
                    readFromCache<T>(cacheKey, url)?.let {
                        circuitBreaker.onSuccess()
                        return it
                    }
                }
                circuitBreaker.onFailure()
                Outcome.Failure(errorMapper.fromHttp(statusCode, safeBody(response), url, headersMap))
            }
        }
    }

    private suspend fun <T> handleEnvelope(
        cacheKey: String,
        url: String,
        response: HttpResponse,
        envelopeHandler: (suspend (HttpResponse) -> ApiResponse<T>),
        meta: Meta
    ): Outcome<T> {
        val envelope = envelopeHandler(response)
        if (envelope.hasError || envelope.data == null) {
            return Outcome.Failure(
                AppError.Business(
                    envelope.message ?: "Business error",
                    payload = envelope.meta
                )
            )
        }
        val metaWithPagination = envelope.meta?.toPageInfo()?.let { meta.copy(pagination = it) } ?: meta
        cacheIfNeeded(cacheKey, url, response, envelope.data, metaWithPagination)
        return Outcome.Success(envelope.data, metaWithPagination)
    }

    private suspend fun <T> handleRaw(
        cacheKey: String,
        url: String,
        response: HttpResponse,
        parser: (suspend (HttpResponse) -> T),
        meta: Meta
    ): Outcome<T> {
        val value = parser(response)
        cacheIfNeeded(cacheKey, url, response, value, meta)
        return Outcome.Success(value, meta)
    }

    private fun extractMeta(
        response: HttpResponse,
        latencyMillis: Long,
        pagination: PageInfo?,
        headers: Map<String, String>
    ): Meta =
        Meta(
            statusCode = response.status.value,
            etag = response.headers[NetHeaders.ETAG],
            requestId = response.headers[NetHeaders.X_REQUEST_ID],
            pagination = pagination,
            extras = headers,
            fromCache = false,
            latencyMillis = latencyMillis,
            receivedAtMillis = System.currentTimeMillis()
        )

    private suspend fun safeBody(response: HttpResponse): String? =
        runCatching { response.bodyAsText() }.getOrNull()

    private fun <T> cacheIfNeeded(
        cacheKey: String,
        url: String,
        response: HttpResponse,
        value: T,
        meta: Meta
    ) {
        if (config.cachePolicy == CachePolicy.NoCache) return
        response.headers[NetHeaders.ETAG]?.let { cacheRepository.writeEtag(cacheKey, url, it) }
        cacheRepository.write(cacheKey, url, value, meta.copy(fromCache = false))
    }

    private fun <T> readFromCache(cacheKey: String, url: String): Outcome<T>? =
        cacheRepository.read<T>(cacheKey, url)?.let { cached -> cached.toOutcome() }

    private fun <T> CachedEntry<T>.toOutcome(): Outcome<T> = Outcome.Success(value, meta)

    private fun cacheKey(method: HttpMethod, suffix: String): String = "${method.value}|$suffix"

    private fun HttpResponse.toPageInfo(): PageInfo? {
        val page = headers["X-Page"]?.toIntOrNull()
        val pageSize = headers["X-Page-Size"]?.toIntOrNull()
        val nextPage = headers["X-Next-Page"]?.toIntOrNull()
        val prevPage = headers["X-Prev-Page"]?.toIntOrNull()
        val total = headers["X-Total"]?.toLongOrNull()
        val nextCursor = headers["X-Next-Cursor"]
        val prevCursor = headers["X-Prev-Cursor"]
        return if (listOf(page, pageSize, nextPage, prevPage, total, nextCursor, prevCursor).any { it != null }) {
            PageInfo(
                page = page,
                pageSize = pageSize,
                nextPage = nextPage,
                previousPage = prevPage,
                total = total,
                nextCursor = nextCursor,
                previousCursor = prevCursor
            )
        } else {
            null
        }
    }

    private fun Map<String, String>.toPageInfo(): PageInfo? {
        if (isEmpty()) return null
        val normalized = entries.associate { it.key.lowercase(Locale.ROOT) to it.value }
        val page = normalized["page"]?.toIntOrNull()
        val pageSize = normalized["pagesize"]?.toIntOrNull() ?: normalized["page_size"]?.toIntOrNull()
        val nextPage = normalized["nextpage"]?.toIntOrNull() ?: normalized["next_page"]?.toIntOrNull()
        val prevPage = normalized["prevpage"]?.toIntOrNull() ?: normalized["prev_page"]?.toIntOrNull()
        val total = normalized["total"]?.toLongOrNull()
        val nextCursor = normalized["nextcursor"] ?: normalized["next_cursor"]
        val prevCursor = normalized["prevcursor"] ?: normalized["prev_cursor"]
        return if (listOf(page, pageSize, nextPage, prevPage, total, nextCursor, prevCursor).any { it != null }) {
            PageInfo(
                page = page,
                pageSize = pageSize,
                nextPage = nextPage,
                previousPage = prevPage,
                total = total,
                nextCursor = nextCursor,
                previousCursor = prevCursor
            )
        } else {
            null
        }
    }
}