package com.zar.zarpakhsh.data.mappers

import com.zar.zarpakhsh.data.models.LoginResponse

fun LoginResponse.toUser(): User {
    return User(
        id = userId,
        username = username,
        email = email,
        token = token
    )
}