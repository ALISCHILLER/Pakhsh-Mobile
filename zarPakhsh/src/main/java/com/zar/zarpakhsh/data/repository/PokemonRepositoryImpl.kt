package com.zar.zarpakhsh.data.repository


import com.zar.core.base.BaseRepository
import com.zar.core.base.map
import com.zar.core.data.network.handler.NetworkHandler
import com.zar.core.data.network.error.NetworkResult
import com.zar.zarpakhsh.data.mappers.toDomain
import com.zar.zarpakhsh.data.models.PokemonResponse
import com.zar.zarpakhsh.domain.entities.Pokemon
import com.zar.zarpakhsh.domain.repository.PokemonRepository
import com.zar.zarpakhsh.data.remote.ApiEndpoints
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PokemonRepositoryImpl(
    networkHandler: NetworkHandler,
) : BaseRepository(networkHandler), PokemonRepository {

    override suspend fun getDitto(): Flow<NetworkResult<Pokemon>> {
        return getAsFlow<PokemonResponse>(ApiEndpoints.POKEMON_DITTO)
            .map { result ->
                result.map { response ->
                    val domainModel = response.toDomain()
                    domainModel
                }
            }
    }

//    private fun PokemonResponse.toEntity(): PokemonEntity {
//        return PokemonEntity(
//            name = this.name,
//            typeNames = this.types.joinToString(", ") { it.type.name },
//            abilityNames = this.abilities.joinToString(", ") { it.ability.name },
//            weight = this.weight,
//            height = this.height,
//            spriteFrontDefault = this.sprites.front_default
//        )
//    }
}