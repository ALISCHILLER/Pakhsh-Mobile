package com.zar.zarpakhsh.di

import com.zar.core.di.NetworkModule
import com.zar.core.di.utilsModule
import org.koin.dsl.module

val appModule = module {
    includes(
        listOf(
            coreModule,
            NetworkModule,
            utilsModule,
            viewModelModule,
            repositoryModule,
            useCaseModule,
            dataLocalModule
        )
    )
}