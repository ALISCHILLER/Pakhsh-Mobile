package com.msa.visitApp.di

import androidx.room.Room
import com.msa.visitApp.core.data.local.VisitDatabase

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
    single { get<VisitDatabase>().visitDao() }

}