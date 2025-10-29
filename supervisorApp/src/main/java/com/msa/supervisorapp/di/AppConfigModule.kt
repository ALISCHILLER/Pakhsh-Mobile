package com.msa.supervisorapp.di

import com.msa.core.common.config.AppConfig
import com.msa.supervisorapp.config.supervisorappConfig
import org.koin.dsl.module

val supervisorappConfigModule = module {
    single<AppConfig> { supervisorappConfig }
}