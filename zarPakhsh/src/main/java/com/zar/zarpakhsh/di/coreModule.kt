package com.zar.zarpakhsh.di

import android.content.Context
import com.zar.core.config.AppConfig
import com.zar.zarpakhsh.data.local.storage.LocalDataSourceAuth
import com.zar.zarpakhsh.data.remote.RemoteDataSourceAuth
import com.zar.zarpakhsh.utils.config.AppConfigZar
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val coreModule = module {
    // Singleton برای LocalDataSourceAuth
    single { LocalDataSourceAuth(get()) }
    // Singleton برای RemoteDataSourceAuth
    single { RemoteDataSourceAuth(get()) }
    single<Context> { androidApplication() }
    single<AppConfig> { AppConfigZar }
}