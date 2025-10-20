package com.msa.core.config

import com.msa.core.common.config.AppConfig as SharedAppConfig


/**
 * Legacy alias over the shared [SharedAppConfig] contract so existing core code can keep
 * referring to `appFlavor`/`databaseName` while all modules share a single definition.
 */
interface AppConfig : SharedAppConfig {
    val appFlavor: String
        get() = flavorName

    val databaseName: String
        get() = dbName
}
