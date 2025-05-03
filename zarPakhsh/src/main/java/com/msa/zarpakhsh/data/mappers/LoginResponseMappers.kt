package com.msa.zarpakhsh.data.mappers

import com.msa.zarpakhsh.data.models.LoginResponse
import com.msa.zarpakhsh.domain.entities.User

fun LoginResponse.toUser(): User {
    return User(
        id = this.userId,
        username = this.username,
        email = this.email,
        token = this.token
    )
}