package com.msa.supervisorapp.di

import com.msa.core.common.config.AppConfig
import com.msa.supervisorApp.config.SupervisorAppConfig
import org.koin.dsl.module

val supervisorAppConfigModule = module {
    single<AppConfig> { SupervisorAppConfig }
}