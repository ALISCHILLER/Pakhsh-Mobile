package com.zar.core.base



import com.zar.core.data.network.common.NetworkHandler
import com.zar.core.data.network.result.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
open class BaseRepository(
    protected val networkHandler: NetworkHandler = NetworkHandler
) {
    /**
     * پل امن برای دسترسی به عضو protected از درون public inline functions.
     * @PublishedApi باعث می‌شود استفاده از آن در inline public مجاز باشد.
     */
    @PublishedApi
    internal fun handler(): NetworkHandler = networkHandler

    /**
     * نسخهٔ داخلیِ asFlow تا public inlineها (مثل getAsFlow/…) بتوانند آن‌را صدا بزنند
     * بدون نقض محدودیت visibility.
     */
    @PublishedApi
    internal fun <T> asFlowInternal(block: suspend () -> NetworkResult<T>): Flow<NetworkResult<T>> =
        flow {
            emit(NetworkResult.Loading)
            emit(block())
        }.catch { e ->
            Timber.e(e, "Error during network flow call")
            // دسترسی به context از طریق handler() که @PublishedApi internal است
            emit(NetworkResult.Error.fromException(e, handler().appContext))
        }.flowOn(Dispatchers.IO)
            .conflate()

    // ---------- Suspend wrappers ----------

    suspend inline fun <reified T> get(url: String): NetworkResult<T> =
        handler().get(url)

    suspend inline fun <reified Req, reified Res> post(url: String, body: Req): NetworkResult<Res> =
        handler().post(url, body)

    suspend inline fun <reified Req, reified Res> put(url: String, body: Req): NetworkResult<Res> =
        handler().put(url, body)

    suspend inline fun <reified Req, reified Res> patch(url: String, body: Req): NetworkResult<Res> =
        handler().patch(url, body)

    suspend inline fun <reified T> delete(url: String): NetworkResult<T> =
        handler().delete(url)

    suspend fun head(url: String): NetworkResult<Unit> =
        handler().head(url)

    // ---------- Flow variants (Loading → Result) ----------
    // این‌ها public inline هستن ولی فقط به توابع @PublishedApi internal دسترسی می‌زنن

    inline fun <reified T> getAsFlow(url: String): Flow<NetworkResult<T>> =
        asFlowInternal { handler().get<T>(url) }

    inline fun <reified Req, reified Res> postAsFlow(url: String, body: Req): Flow<NetworkResult<Res>> =
        asFlowInternal { handler().post<Req, Res>(url, body) }

    inline fun <reified Req, reified Res> putAsFlow(url: String, body: Req): Flow<NetworkResult<Res>> =
        asFlowInternal { handler().put<Req, Res>(url, body) }

    inline fun <reified Req, reified Res> patchAsFlow(url: String, body: Req): Flow<NetworkResult<Res>> =
        asFlowInternal { handler().patch<Req, Res>(url, body) }

    inline fun <reified T> deleteAsFlow(url: String): Flow<NetworkResult<T>> =
        asFlowInternal { handler().delete<T>(url) }

    fun headAsFlow(url: String): Flow<NetworkResult<Unit>> =
        asFlowInternal { handler().head(url) }
}
