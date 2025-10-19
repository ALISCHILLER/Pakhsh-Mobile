package com.msa.core.ui.extensions

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** کنترل ساده Snackbar به‌همراه Scope. */
fun SnackbarHostState.showSnackbarSafe(
    message: String,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Short,
    scope: CoroutineScope
) {
    scope.launch {
        showSnackbar(message = message, actionLabel = actionLabel, duration = duration)
    }
}

/** ساخت جفت [SnackbarHostState] و [CoroutineScope] برای استفاده در Compose. */
@Composable
fun rememberSnackbarController(): Pair<SnackbarHostState, CoroutineScope> {
    val hostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    return hostState to scope
}