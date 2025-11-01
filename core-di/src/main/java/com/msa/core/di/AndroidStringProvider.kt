package com.msa.core.di

import android.content.Context
import com.msa.core.common.text.StringProvider
import com.msa.core.di.R
import com.msa.core.network.error.NetworkStringRes

class AndroidStringProvider(
    private val context: Context,
) : StringProvider {

    override fun get(id: Int): String = context.getString(resolve(id))

    override fun format(id: Int, vararg args: Any): String = context.getString(resolve(id), *args)

    private fun resolve(id: Int): Int = when (id) {
        NetworkStringRes.ERR_TIMEOUT -> R.string.core_network_err_timeout
        NetworkStringRes.ERR_NETWORK -> R.string.core_network_err_network
        NetworkStringRes.ERR_PARSING -> R.string.core_network_err_parsing
        NetworkStringRes.ERR_AUTH -> R.string.core_network_err_auth
        NetworkStringRes.ERR_CLIENT -> R.string.core_network_err_client
        NetworkStringRes.ERR_SERVER -> R.string.core_network_err_server
        else -> id.takeIf { it != 0 } ?: R.string.core_network_err_unknown
    }
}