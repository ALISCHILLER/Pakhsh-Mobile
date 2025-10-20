package com.msa.core.data.network.common


import android.content.Context
import androidx.annotation.StringRes


interface StringProvider {
    fun get(@StringRes resId: Int): String
    fun get(@StringRes resId: Int, vararg args: Any): String
    fun getOrDefault(@StringRes resId: Int?, default: String): String
}


class AndroidStringProvider(private val context: Context) : StringProvider {
    override fun get(resId: Int): String = context.getString(resId)

    override fun get(resId: Int, vararg args: Any): String = context.getString(resId, *args)

    override fun getOrDefault(resId: Int?, default: String): String =
        resId?.let { runCatching { context.getString(it) }.getOrNull() } ?: default
}