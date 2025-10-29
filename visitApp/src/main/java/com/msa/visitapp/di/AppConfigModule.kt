package com.msa.visitapp.di

import com.msa.core.common.config.AppConfig
import com.msa.visitApp.config.VisitAppConfig
import org.koin.dsl.module

val visitAppConfigModule = module {
    single<AppConfig> { VisitAppConfig }
}