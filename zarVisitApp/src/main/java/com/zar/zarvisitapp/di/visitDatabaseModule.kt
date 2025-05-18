package com.zar.zarvisitapp.di

import android.content.Context
import androidx.room.Room
import com.zar.zarvisitapp.data.local.VisitDatabase

import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val visitDatabaseModule = module {

    // VisitDatabase
    single {
        Room.databaseBuilder(
            androidContext(),
            VisitDatabase::class.java,
            "visit_database"
        )
            //.addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }

    // DAO ها
    single { get<VisitDatabase>().userDao() }
    single { get<VisitDatabase>().visitDao() }
    single { get<VisitDatabase>().productGroupDao() }
    single { get<VisitDatabase>().productDao() }
    single { get<VisitDatabase>().productUnitDao() }
    single { get<VisitDatabase>().customerDao() }
    single { get<VisitDatabase>().fcmMessageDao() }
}