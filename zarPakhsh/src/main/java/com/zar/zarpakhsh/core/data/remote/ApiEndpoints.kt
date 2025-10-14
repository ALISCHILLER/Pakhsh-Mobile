package com.zar.zarpakhsh.data.remote

import com.zar.zarpakhsh.data.local.dao.ProductGroupDao


object ApiEndpoints {
    const val BASE_URL = "https://pokeapi.co/api/v2/"
    const val LOGIN = "$BASE_URL/auth/login"
    const val LOGOUT = "$BASE_URL/auth/logout"
    const val PROFILE = "$BASE_URL/user/profile"
    const val CUSTOMER_LIST = "$BASE_URL/user/profile"
    const val Products = "$BASE_URL/user/profile"
    const val ProductGroups = "$BASE_URL/user/profile"

    const val POKEMON_DITTO = "$BASE_URL/pokemon/ditto"
}
