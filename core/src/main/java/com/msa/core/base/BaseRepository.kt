package com.msa.core.base



import com.msa.core.common.coroutines.CoroutineDispatchers
import com.msa.core.common.coroutines.DefaultCoroutineDispatchers
import com.msa.core.data.network.client.NetworkClient
import com.msa.core.data.network.result.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber


open class BaseRepository(
    protected val networkClient: NetworkClient,
    private val dispatchers: CoroutineDispatchers = DefaultCoroutineDispatchers(),
) {

    @PublishedApi
    internal fun client(): NetworkClient = networkClient


    @PublishedApi
    internal fun <T> asFlowInternal(block: suspend () -> NetworkResult<T>): Flow<NetworkResult<T>> =
        flow {
            emit(NetworkResult.Loading)
            emit(block())
        }.catch { e ->
            Timber.e(e, "Error during network flow call")
            emit(networkClient.asError(e))
        }.flowOn(dispatchers.io)
            .conflate()

    // ---------- Suspend wrappers ----------

    suspend inline fun <reified T> get(url: String): NetworkResult<T> =
        client().get(url)

    suspend inline fun <reified Req, reified Res> post(url: String, body: Req): NetworkResult<Res> =
        client().post(url, body)

    suspend inline fun <reified Req, reified Res> put(url: String, body: Req): NetworkResult<Res> =
        client().put(url, body)

    suspend inline fun <reified Req, reified Res> patch(url: String, body: Req): NetworkResult<Res> =
        client().patch(url, body)

    suspend inline fun <reified T> delete(url: String): NetworkResult<T> =
        client().delete(url)

    suspend fun head(url: String): NetworkResult<Unit> =
        client().head(url)

    // ---------- Flow variants (Loading → Result) ----------

    inline fun <reified T> getAsFlow(url: String): Flow<NetworkResult<T>> =
        asFlowInternal { client().get<T>(url) }

    inline fun <reified Req, reified Res> postAsFlow(url: String, body: Req): Flow<NetworkResult<Res>> =
        asFlowInternal { client().post<Req, Res>(url, body) }

    inline fun <reified Req, reified Res> putAsFlow(url: String, body: Req): Flow<NetworkResult<Res>> =
        asFlowInternal { client().put<Req, Res>(url, body) }

    inline fun <reified Req, reified Res> patchAsFlow(url: String, body: Req): Flow<NetworkResult<Res>> =
        asFlowInternal { client().patch<Req, Res>(url, body) }

    inline fun <reified T> deleteAsFlow(url: String): Flow<NetworkResult<T>> =
        asFlowInternal { client().delete<T>(url) }

    fun headAsFlow(url: String): Flow<NetworkResult<Unit>> =
        asFlowInternal { client().head(url) }
}
