package com.msa.zarpakhsh.di

import com.msa.core.di.NetworkModule
import com.msa.core.di.loggerModule
import com.msa.core.di.utilsModule
import org.koin.core.module.Module

val appModule = listOf(
    CoreModule,
    loggerModule,
    NetworkModule,
    utilsModule,
    viewModelModule,
    repositoryModule
)