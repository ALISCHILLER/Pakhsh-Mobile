package com.msa.core.ui.extensions

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * اکستنشن‌های پرکاربرد Modifier و الگوهای رفتاری UI.
 */

/** جلوگیری از کلیک‌های سریع (debounce). */
fun Modifier.debouncedClickable(
    debounceTime: Long = 600L,
    onClick: () -> Unit
): Modifier = composed {
    var lastClickTime by remember { mutableStateOf(0L) }
    val interactionSource = remember { MutableInteractionSource() }

    clickable(interactionSource = interactionSource, indication = null) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= debounceTime) {
            lastClickTime = currentTime
            onClick()
        }
    }
}

/** تغییر شفافیت بر اساس وضعیت فعال/غیرفعال. */
fun Modifier.alphaIfDisabled(isEnabled: Boolean, disabledAlpha: Float = 0.5f): Modifier =
    alpha(if (isEnabled) 1f else disabledAlpha)

/** تبدیل dp به px با درنظر گرفتن density. */
@Composable
fun Dp.toPx(): Float = with(LocalDensity.current) { this@toPx.toPx() }

/** اجرای تابع پس از تاخیر مشخص. */
@Composable
fun OnDelay(timeMillis: Long, onDelayFinish: () -> Unit) {
    LaunchedEffect(timeMillis, onDelayFinish) {
        delay(timeMillis)
        onDelayFinish()
    }
}

/** padding یکسان برای همه جهات. */
fun Modifier.paddingAll(padding: Dp): Modifier = padding(padding)

/** padding فقط افقی. */
fun Modifier.paddingHorizontal(horizontal: Dp): Modifier = padding(start = horizontal, end = horizontal)

/** padding فقط عمودی. */
fun Modifier.paddingVertical(vertical: Dp): Modifier = padding(top = vertical, bottom = vertical)

/** شبیه‌سازی margin با padding والد. */
fun Modifier.margin(all: Dp): Modifier = padding(all)

/** رسم حاشیه گرد همراه با clip متناظر. */
fun Modifier.roundedBorder(
    borderColor: androidx.compose.ui.graphics.Color,
    borderWidth: Dp = 1.dp,
    cornerRadius: Dp = 8.dp
): Modifier =
    border(borderWidth, borderColor, RoundedCornerShape(cornerRadius)).clip(RoundedCornerShape(cornerRadius))

@Preview
@Composable
private fun UiExtensionsPreview() {
    Button(
        onClick = {},
        modifier = Modifier
            .debouncedClickable { }
            .alphaIfDisabled(isEnabled = false)
            .paddingAll(12.dp)
            .roundedBorder(borderColor = androidx.compose.ui.graphics.Color.Red)
    ) {
        Text(text = "کلیک")
    }
}