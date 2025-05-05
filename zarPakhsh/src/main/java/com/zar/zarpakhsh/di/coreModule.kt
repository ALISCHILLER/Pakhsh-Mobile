package com.zar.zarpakhsh.di

import android.content.Context
import com.zar.core.config.AppConfig
import com.zar.core.data.network.handler.NetworkHandler
import com.zar.core.data.network.model.NetworkConfig
import com.zar.core.data.network.utils.NetworkStatusMonitor
import com.zar.zarpakhsh.data.local.storage.LocalDataSourceAuth
import com.zar.zarpakhsh.utils.config.AppConfigZar
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModule = module {
    // Context
    single { androidContext().applicationContext as Context }

    // NetworkConfig
    single { NetworkConfig.DEFAULT }

    // NetworkStatusMonitor
    factory { (context: Context) -> NetworkStatusMonitor(context) }

    // NetworkHandler
    single {
        val context: Context = get()
        val monitor: NetworkStatusMonitor = get()
        val config: NetworkConfig = get()
        NetworkHandler.initialize(context, monitor, config)
        NetworkHandler
    }
    single<AppConfig> { AppConfigZar }
}