package com.msa.core.utils.extension

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

/**
 * توابع Extension برای راحت‌تر کردن استایل‌دهی Modifier در Jetpack Compose
 */

/**
 * ایجاد Padding یکسان در تمام جهات
 */
fun Modifier.paddingAll(padding: Dp): Modifier = this.then(
    Modifier.padding(padding)
)

/**
 * ایجاد Padding به صورت فقط افقی (Horizontal)
 */
fun Modifier.paddingHorizontal(horizontal: Dp): Modifier = this.then(
    Modifier.padding(start = horizontal, end = horizontal)
)

/**
 * ایجاد Padding به صورت فقط عمودی (Vertical)
 */
fun Modifier.paddingVertical(vertical: Dp): Modifier = this.then(
    Modifier.padding(top = vertical, bottom = vertical)
)

/**
 * ایجاد Margin (که در واقع همون Padding در Parent هست در Compose)
 */
fun Modifier.margin(all: Dp): Modifier = this.then(
    Modifier.padding(all)
)

/**
 * قرار دادن Border با رنگ و شعاع گردی گوشه‌ها
 */
fun Modifier.roundedBorder(
    borderColor: Color,
    borderWidth: Dp = 1.dp,
    cornerRadius: Dp = 8.dp
): Modifier = this
    .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius))
    .clip(RoundedCornerShape(cornerRadius))

@Preview
@Composable
private fun ModifierExtensionsPreview() {
    Box(
        modifier = Modifier
            .paddingAll(16.dp)
            .roundedBorder(borderColor = Color.Red, borderWidth = 2.dp, cornerRadius = 12.dp)
    )
}