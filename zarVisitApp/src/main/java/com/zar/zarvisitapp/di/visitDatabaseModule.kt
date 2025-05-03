package com.zar.zarvisitapp.di

import android.content.Context
import com.zar.zarvisitapp.data.local.VisitDatabase

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val visitDatabaseModule = module {

    // VisitDatabase
    single<VisitDatabase> {
        VisitDatabase.create(get())
    }

    // VisitDao
    factory {
        get<VisitDatabase>().visitDao()
    }

    // UserDao (مشترک)
    factory {
        get<VisitDatabase>().userDao()
    }
}