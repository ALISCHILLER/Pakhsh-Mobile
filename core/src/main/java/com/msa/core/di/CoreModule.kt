package com.msa.core.di


import com.msa.core.common.coroutines.CoroutineDispatchers
import com.msa.core.common.coroutines.DefaultCoroutineDispatchers
import com.msa.core.common.time.Clock
import com.msa.core.data.network.client.HttpClientFactory
import com.msa.core.data.network.client.NetworkClient
import com.msa.core.data.network.common.AndroidStringProvider
import com.msa.core.data.network.common.StringProvider
import com.msa.core.data.network.error.ErrorMapper
import com.msa.core.data.network.model.NetworkConfig
import com.msa.core.data.network.utils.NetworkStatusMonitor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModule = module {

    // اگر AppConfig را در ماژول اپ تزریق نکرده‌ای، اینجا بساز/تزریق کن:
    // single<AppConfig> { YourAppConfigImpl(androidContext()) }

    // پیکربندی شبکه (پیش‌فرض)
    single { NetworkConfig.DEFAULT }

    // StringProvider برای پیام‌های خطا و … (وابسته به context)
    single<StringProvider> { AndroidStringProvider(androidContext()) }

    // نگاشت خطاها (به StringProvider نیاز دارد)
    single { ErrorMapper(get()) }


    single<CoroutineDispatchers> { DefaultCoroutineDispatchers() }
    single { Clock.System }
    // مانیتور وضعیت شبکه — از applicationContext استفاده کن تا لیک نشه
    single { NetworkStatusMonitor(androidContext().applicationContext) }

    // HttpClient(Ktor) — از NetworkConfig می‌گیرد
    single { HttpClientFactory.create(androidContext().applicationContext, get<NetworkConfig>()) }

    // کلاینت شبکه‌ی سطح بالا
    single {
        NetworkClient(
            httpClient = get(),          // HttpClient
            statusMonitor = get(),       // NetworkStatusMonitor
            stringProvider = get(),      // StringProvider
            errorMapper = get(),         // ErrorMapper
            dispatchers = get(),
            clock = get(),
        )
    }
}
