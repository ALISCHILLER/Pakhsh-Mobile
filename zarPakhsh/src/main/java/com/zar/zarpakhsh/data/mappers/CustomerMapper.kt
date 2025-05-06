package com.zar.zarpakhsh.data.mappers


import com.zar.zarpakhsh.data.local.entity.CustomerModelEntity
import com.zar.zarpakhsh.data.models.CustomerResponse
import com.zar.zarpakhsh.domain.entities.Customer

// Response → Domain
fun CustomerResponse.toDomain(): Customer {
    return Customer(
        id = id,
        name = name,
        email = email ?: "",
        phone = phone ?: "",
        address = address ?: "",
        city = city ?: "",
        state = state ?: "",
        country = country ?: "",
        postalCode = postalCode ?: ""
    )
}

// Domain → Local Entity
fun Customer.toEntity(): CustomerModelEntity {
    return CustomerModelEntity(
        id = id,
        name = name,
        email = email,
        phone = phone,
        address = address,
        city = city,
        state = state,
        country = country,
        postalCode = postalCode
    )
}

// Local Entity → Domain
fun CustomerModelEntity.toDomain(): Customer {
    return Customer(
        id = id,
        name = name,
        email = email,
        phone = phone,
        address = address,
        city = city,
        state = state,
        country = country,
        postalCode = postalCode
    )
}
