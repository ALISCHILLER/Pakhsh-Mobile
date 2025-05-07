package com.zar.zarpakhsh.di

import com.zar.zarpakhsh.data.repository.AuthRepositoryImpl
import com.zar.zarpakhsh.data.repository.PokemonRepositoryImpl
import com.zar.zarpakhsh.domain.repository.AuthRepository
import com.zar.zarpakhsh.domain.repository.PokemonRepository
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

val repositoryModule = module {

        // Auth Repository
//        single<AuthRepository> {
//            AuthRepositoryImpl(get())
//        }

        // Pokemon Repository
        single<PokemonRepository> {
            PokemonRepositoryImpl(get())
        }withOptions {
            createdAtStart()
        }

}