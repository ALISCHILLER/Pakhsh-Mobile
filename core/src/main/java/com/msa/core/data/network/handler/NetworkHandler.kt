package com.msa.core.data.network.handler

import android.util.Log // استفاده از Log استاندارد اندروید
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.* // شامل ContentNegotiation, HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.* // شامل Logging, LogLevel, Logger
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.IOException // Import necessary exceptions
import kotlinx.coroutines.TimeoutCancellationException
/**
 * یک شیء Singleton برای مدیریت و پیکربندی HttpClient Ktor و انجام درخواست‌های شبکه.
 * مسئول اجرای درخواست‌ها و کپسوله کردن نتیجه در NetworkResult است.
 */
object NetworkHandler {

    /**
     * HttpClient با تنظیمات پیشرفته.
     */
    private val httpClient = HttpClient(OkHttp) {
        // Serializer برای تبدیل JSON با Kotlinx Serialization
        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true // فرمت دهی زیبای JSON در لاگ
                    isLenient = true // انعطاف پذیری بیشتر در خواندن JSON
                    ignoreUnknownKeys = true // نادیده گرفتن فیلدهای ناشناخته در JSON
                    encodeDefaults = false // عدم نمایش مقادیر پیش‌فرض در خروجی JSON
                }
            )
        }

        // لاگ‌گیری برای Debugging
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.v("NetworkHandler", message) // استفاده از Log.v برای جزئیات بیشتر
                }
            }
            level = LogLevel.ALL // لاگ‌گیری همه جزئیات شامل Header, Body و ...
        }

        // مدیریت Timeout
        install(HttpTimeout) { // استفاده از HttpTimeout از plugins.*
            requestTimeoutMillis = 15_000L // 15 ثانیه برای کل درخواست
            connectTimeoutMillis = 15_000L // 15 ثانیه برای اتصال
            socketTimeoutMillis = 15_000L // 15 ثانیه برای خواندن/نوشتن داده
        }

        // مشاهده وضعیت پاسخ‌ها (اختیاری برای لاگ‌گیری یا بررسی سریع وضعیت)
        install(ResponseObserver) {
            onResponse { response ->
                Log.d("HTTP Status", "${response.status.value}")
                // می‌توانید منطق بیشتری برای بررسی کد وضعیت HTTP اینجا اضافه کنید
                // مثلاً برای خطاهای 401 Unauthorized
            }
        }

        // Headerهای مشترک برای همه درخواست‌ها
        defaultRequest {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json) // پذیرش پاسخ با فرمت JSON
            // headers.append(HttpHeaders.Authorization, "Bearer your_token") // مثال: اضافه کردن توکن احراز هویت
        }

        // مدیریت خطاهای پاسخ HTTP (مانند کدهای 4xx, 5xx)
        // این یک راه برای تبدیل خودکار کدهای وضعیت به استثناء است
        // install(HttpBadResponseStatus) // می تواند استثناء های HttpStatusCodeException را پرتاب کند
    }

    /**
     * تابع عمومی برای دسترسی به HttpClient.
     * اگر نیاز به پیکربندی‌های خاص در لایه‌های دیگر دارید، می‌توانید از این تابع استفاده کنید.
     */
    fun getHttpClient(): HttpClient {
        return httpClient
    }

    /**
     * تابع مرکزی برای انجام درخواست‌های شبکه و مدیریت خطاهای احتمالی به صورت ایمن.
     * این تابع هر بلاک کدی که یک درخواست شبکه را انجام می‌دهد می‌پذیرد و نتیجه را در NetworkResult کپسوله می‌کند.
     */
    suspend fun <T> safeApiCall(apiCall: suspend () -> T): NetworkResult<T> {
        return try {
            withContext(Dispatchers.IO) {
                val response = apiCall()
                NetworkResult.Success(response)
            }
        } catch (exception: Exception) { // Catching broad Exception - Consider catching more specific types
            Log.e("NetworkHandler", "Error occurred during safeApiCall: ${exception.message}")
            // ارسال Exception به ErrorHandler برای پردازش و ایجاد NetworkResult.Error
            ErrorHandler.handleNetworkError(exception)
        }
    }

    // توابع کمکی برای درخواست‌های HTTP مختلف که از safeApiCall استفاده می‌کنند
    // از inline و reified استفاده می‌شود تا بتوان نوع T را در زمان کامپایل مشخص کرد و از body<T>() استفاده نمود.

    /**
     * تابعی برای انجام درخواست GET.
     */
    suspend inline fun <reified T> getRequest(url: String): NetworkResult<T> {
        return safeApiCall {
            getHttpClient().get(url).body<T>() // استفاده از body<T>() برای دریافت بدنه پاسخ به صورت مدل T
        }
    }

    /**
     * تابعی برای انجام درخواست POST.
     */
    suspend inline fun <reified T> postRequest(url: String, body: Any): NetworkResult<T> {
        return safeApiCall {
            getHttpClient().post(url) {
                setBody(body) // تنظیم بدنه درخواست
            }.body<T>()
        }
    }

    /**
     * تابعی برای انجام درخواست PUT.
     */
    suspend inline fun <reified T> putRequest(url: String, body: Any): NetworkResult<T> {
        return safeApiCall {
            getHttpClient().put(url) {
                setBody(body)
            }.body<T>()
        }
    }

    /**
     * تابعی برای انجام درخواست DELETE.
     * فرض بر این است که پاسخ DELETE نیازی به بدنه خاصی ندارد (T=Unit).
     */
    suspend inline fun <reified T> deleteRequest(url: String): NetworkResult<T> {
        return safeApiCall {
            getHttpClient().delete(url).body<T>()
        }
    }

    // می توانید توابع دیگری برای PATCH, HEAD و ... اضافه کنید
}