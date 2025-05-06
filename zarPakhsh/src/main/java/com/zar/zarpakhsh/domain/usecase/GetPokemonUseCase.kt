package com.zar.zarpakhsh.domain.usecase


import com.zar.core.data.network.error.NetworkResult
import com.zar.zarpakhsh.domain.entities.Pokemon
import com.zar.zarpakhsh.domain.repository.PokemonRepository
import kotlinx.coroutines.flow.Flow

class GetPokemonUseCase(
    private val repository: PokemonRepository
) {
    suspend operator fun invoke(): Flow<NetworkResult<Pokemon>>? {
        return null
    }
}