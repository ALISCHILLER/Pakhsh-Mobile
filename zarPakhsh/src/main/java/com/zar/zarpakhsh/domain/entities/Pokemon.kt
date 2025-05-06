package com.zar.zarpakhsh.domain.entities

data class Pokemon(
    val name: String,
    val typeNames: List<String>,
    val abilityNames: List<String>,
    val weight: Int,
    val height: Int,
    val spriteFrontDefault: String
)