package com.zar.zarpakhsh.data.mappers

import com.zar.zarpakhsh.data.models.LoginResponse
import com.zar.zarpakhsh.domain.entities.User

fun LoginResponse.toUser(): User {
    return User(
        id = userId,
        username = username,
        email = email,
        token = token
    )
}