package com.msa.zarpakhsh.di

import com.msa.core.di.NetworkModule
import com.msa.core.di.utilsModule
import org.koin.core.module.Module

val appModule = listOf(
    coreModule,
    NetworkModule,
    utilsModule,
    viewModelModule,
    repositoryModule,
    useCaseModule,
    dataLocalModule
)