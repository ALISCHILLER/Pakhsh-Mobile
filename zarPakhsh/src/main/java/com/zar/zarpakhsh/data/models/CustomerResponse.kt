package com.zar.zarpakhsh.data.models

import com.zar.zarpakhsh.domain.entities.Customer

data class CustomerResponse(
    val customer: List<Customer>
)