package com.msa.core.common.text


interface StringProvider {
    fun get(id: Int): String
    fun format(id: Int, vararg args: Any): String
}