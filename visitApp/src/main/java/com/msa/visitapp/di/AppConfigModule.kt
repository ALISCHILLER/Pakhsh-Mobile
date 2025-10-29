package com.msa.visitapp.di

import com.msa.core.common.config.AppConfig
import com.msa.visitapp.config.visitappConfig
import org.koin.dsl.module

val visitappConfigModule = module {
    single<AppConfig> { visitappConfig }
}