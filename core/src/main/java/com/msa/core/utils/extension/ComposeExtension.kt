package com.msa.core.utils.extension



import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.delay

/**
 * توابع Extension برای Jetpack Compose
 * این فایل توابع کمکی برای مدیریت Modifier و رویدادهای UI در Compose را فراهم می‌کند.
 */

/**
 * کلیک همراه با جلوگیری از دوبار کلیک سریع (Debounce Click)
 * @param debounceTime زمان فاصله بین کلیک‌ها (به میلی‌ثانیه)
 * @param onClick عملی که هنگام کلیک باید انجام شود
 */
fun Modifier.debouncedClickable(
    debounceTime: Long = 600L,
    onClick: () -> Unit
): Modifier = composed {
    var lastClickTime by remember { mutableStateOf(0L) }
    val interactionSource = remember { MutableInteractionSource() }

    this.clickable(
        interactionSource = interactionSource,
        indication = null
    ) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= debounceTime) {
            lastClickTime = currentTime
            onClick()
        }
    }
}

/**
 * تغییر شفافیت (alpha) ویو بر اساس فعال یا غیرفعال بودن
 * @param isEnabled فعال یا غیرفعال بودن
 * @param disabledAlpha میزان شفافیت در حالت غیرفعال
 */
fun Modifier.alphaIfDisabled(
    isEnabled: Boolean,
    disabledAlpha: Float = 0.5f
): Modifier = this.alpha(if (isEnabled) 1f else disabledAlpha)

/**
 * تبدیل Dp به Px
 * @return مقدار معادل به پیکسل
 */
@Composable
fun Dp.toPx(): Float {
    val density = LocalDensity.current
    return with(density) { this@toPx.toPx() }
}

/**
 * اجرای یک تابع بعد از یک مدت زمان مشخص (Delay)
 * @param timeMillis زمان تاخیر بر حسب میلی‌ثانیه
 * @param onDelayFinish تابعی که بعد از پایان تاخیر اجرا می‌شود
 */
@Composable
fun OnDelay(
    timeMillis: Long,
    onDelayFinish: () -> Unit
) {
    LaunchedEffect(key1 = true) {
        delay(timeMillis)
        onDelayFinish()
    }
}


@Preview
@Composable
private fun ComposeExtensionPreview() {

    Button(
        onClick = { /* عملیات */ },
        modifier = Modifier
            .debouncedClickable {
                // فقط یک بار در بازه زمانی کلیک می‌کند
            }
            .alphaIfDisabled(isEnabled = false)
    ) {
        Text("ثبت سفارش")
    }
    OnDelay(timeMillis = 3000L) {
        // بعد ۳ ثانیه اینجا اجرا میشه
    }
}
