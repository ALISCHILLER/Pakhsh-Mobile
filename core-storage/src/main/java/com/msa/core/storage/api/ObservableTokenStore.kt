package com.msa.core.storage.api

import kotlinx.coroutines.flow.Flow

interface ObservableTokenStore : TokenStore {
    val tokens: Flow<TokenSnapshot?>
}