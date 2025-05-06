package com.zar.zarpakhsh.data.models



import kotlinx.serialization.Serializable

@Serializable
data class PokemonResponse(
    val name: String,
    val types: List<TypeInfo>,
    val abilities: List<AbilityInfo>,
    val weight: Int,
    val height: Int,
    val sprites: SpriteInfo
)

@Serializable
data class TypeInfo(
    val type: NamedResource
)

@Serializable
data class AbilityInfo(
    val ability: NamedResource,
    val isHidden: Boolean
)

@Serializable
data class NamedResource(
    val name: String,
    val url: String
)

@Serializable
data class SpriteInfo(
    val front_default: String,
    val front_shiny: String,
    val back_default: String,
    val back_shiny: String
)