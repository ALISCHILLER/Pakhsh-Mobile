package com.msa.distributeapp.di

import com.msa.core.common.config.AppConfig
import com.msa.distributeApp.config.DistributeAppConfig
import org.koin.dsl.module

val distributeAppConfigModule = module {
    single<AppConfig> { DistributeAppConfig }
}