package com.msa.distributeapp.di

import com.msa.core.common.config.AppConfig
import com.msa.distributeapp.config.distributeappConfig
import org.koin.dsl.module

val distributeappConfigModule = module {
    single<AppConfig> { distributeappConfig }
}