package com.zar.zarpakhsh.di


import com.zar.core.di.networkModule
import com.zar.core.di.utilsModule
import org.koin.dsl.module

val appModule = module {
    includes(coreModule)
//    includes(repositoryModule)
//    includes(useCaseModule)
//    includes(viewModelModule)
}