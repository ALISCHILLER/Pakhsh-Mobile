package com.msa.core.utils.extension




import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * تابع Extension برای نمایش ساده‌ی Snackbar
 */

/**
 * نمایش یک Snackbar به سادگی
 * @param message متن پیغام
 * @param actionLabel متن دکمه (اختیاری)
 * @param duration مدت زمان نمایش
 */
fun SnackbarHostState.showSnackbarSafe(
    message: String,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Short,
    scope: CoroutineScope
) {
    scope.launch {
        this@showSnackbarSafe.showSnackbar(
            message = message,
            actionLabel = actionLabel,
            duration = duration
        )
    }
}

/**
 * آماده کردن Scope و استفاده راحت‌تر از showSnackbarSafe
 */
@Composable
fun rememberSnackbarController(): Pair<SnackbarHostState, CoroutineScope> {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    return Pair(snackbarHostState, scope)
}

@Preview
@Composable
private fun SnackbarExtensionPreview() {

    val (snackbarHostState, snackbarScope) = rememberSnackbarController()
    Button(
        onClick = {
            snackbarHostState.showSnackbarSafe(
                message = "عملیات موفقیت‌آمیز بود!",
                actionLabel = "باشه",
                scope = snackbarScope
            )
        }
    ) {
        Text("نمایش پیام")
    }

}