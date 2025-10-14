package com.zar.zarpakhsh.domain.repository

import com.zar.core.data.network.result.NetworkResult
import com.zar.zarpakhsh.domain.entities.Pokemon
import kotlinx.coroutines.flow.Flow

interface PokemonRepository {
    suspend fun getDitto(): Flow<NetworkResult<Pokemon>>
}