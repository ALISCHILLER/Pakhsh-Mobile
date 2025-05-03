package com.zar.zarpakhsh.data.mappers

import com.zar.zarpakhsh.data.models.LoginResponse
import com.zar.zarpakhsh.domain.entities.User

fun LoginResponse.toUser(): User {
    return User(
        id = this.userId,
        username = this.username,
        email = this.email,
        token = this.token
    )
}