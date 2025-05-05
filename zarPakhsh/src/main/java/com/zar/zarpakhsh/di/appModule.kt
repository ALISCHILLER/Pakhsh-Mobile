package com.zar.zarpakhsh.di


import com.zar.core.di.networkModule
import com.zar.core.di.utilsModule
import org.koin.dsl.module

val appModule = module {
    includes(
        listOf(
            coreModule,
            networkModule,
            utilsModule,
            viewModelModule,
            repositoryModule,
            useCaseModule,
        )
    )
}