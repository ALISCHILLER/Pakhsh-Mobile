package com.msa.persistence.di

import androidx.room.Room
import com.msa.core.common.config.AppConfig
import com.msa.core.common.coroutines.CoroutineDispatchers
import com.msa.core.network.auth.TokenStore
import com.msa.core.storage.prefs.BaseSharedPreferences
import com.msa.core.storage.token.TokenStoreEncrypted
import com.msa.persistence.common.device.AndroidDeviceIdProvider
import com.msa.persistence.common.device.DeviceIdProvider
import com.msa.persistence.common.prefs.SessionPrefs
import com.msa.persistence.data.auth.local.datastore.AuthLocalDataSource
import com.msa.persistence.data.auth.local.datastore.AuthSessionStore
import com.msa.persistence.data.auth.local.db.PersistenceDatabase
import com.msa.persistence.data.auth.remote.AuthApi
import com.msa.persistence.data.auth.remote.AuthApiImpl
import com.msa.persistence.data.auth.repo.createAuthRepository
import com.msa.persistence.domain.auth.repository.AuthRepository
import com.msa.persistence.domain.auth.usecase.LoginUseCase
import com.msa.persistence.domain.auth.usecase.LogoutUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val persistenceModule = module {
    single { SessionPrefs(get()) }

    single {
        Room.databaseBuilder(
            androidContext(),
            PersistenceDatabase::class.java,
            PersistenceDatabase.NAME,
        ).fallbackToDestructiveMigration()
            .build()
    }
    single { get<PersistenceDatabase>().authSessionDao() }

    single<TokenStore> { TokenStoreEncrypted(get<BaseSharedPreferences>()) }

    single<DeviceIdProvider> { AndroidDeviceIdProvider(get()) }

    single { AuthLocalDataSource(get(), get(), get<CoroutineDispatchers>()) } bind AuthSessionStore::class

    single<AuthApi> { AuthApiImpl(get(), get<AppConfig>()) }

    single<AuthRepository> {
        createAuthRepository(
            api = get(),
            localDataSource = get(),
            tokenStore = get(),
            clock = get(),
            deviceIdProvider = get(),
        )
    }

    factory { LoginUseCase(get()) }
    factory { LogoutUseCase(get()) }
}