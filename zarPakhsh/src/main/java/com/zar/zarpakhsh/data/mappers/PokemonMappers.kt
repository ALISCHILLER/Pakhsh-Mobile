package com.zar.zarpakhsh.data.mappers

import com.zar.zarpakhsh.data.models.PokemonResponse
import com.zar.zarpakhsh.domain.entities.Pokemon

fun PokemonResponse.toDomain(): Pokemon {
    return Pokemon(
        name = this.name,
        typeNames = this.types.map { it.type.name },
        abilityNames = this.abilities.map { it.ability.name },
        weight = this.weight,
        height = this.height,
        spriteFrontDefault = this.sprites.front_default
    )
}