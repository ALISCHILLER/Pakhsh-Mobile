package com.msa.core.utils.extension

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.DpOffset
import kotlin.math.roundToInt

/**
 * توابع Extension برای انیمیشن‌های پیشرفته در Jetpack Compose
 */

/**
 * انیمیشن FadeIn با امکان تنظیم AnimationSpec سفارشی
 * @param targetAlpha مقدار هدف برای alpha
 * @param durationMillis مدت زمان انیمیشن
 * @param delayMillis تاخیر انیمیشن
 * @param animationSpec تنظیمات سفارشی انیمیشن
 */
fun Modifier.fadeIn(
    targetAlpha: Float = 1f,
    durationMillis: Int = 1000,
    delayMillis: Int = 0,
    animationSpec: AnimationSpec<Float> = tween(durationMillis = durationMillis, delayMillis = delayMillis)
): Modifier = composed {
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = animationSpec
    )
    this.graphicsLayer(alpha = alpha)
}

/**
 * انیمیشن SlideIn با امکان تنظیم AnimationSpec سفارشی
 * @param targetOffset جابجایی نهایی
 * @param durationMillis مدت زمان انیمیشن
 * @param delayMillis تاخیر انیمیشن
 * @param animationSpec تنظیمات سفارشی انیمیشن
 */
fun Modifier.slideIn(

    targetOffset:  DpOffset = DpOffset(0.dp, 0.dp),
    durationMillis: Int = 1000,
    delayMillis: Int = 0,
    animationSpec: AnimationSpec<Float> = tween(durationMillis = durationMillis, delayMillis = delayMillis)
): Modifier = composed {
    val offsetX by animateFloatAsState(
        targetValue = targetOffset.x.value,
        animationSpec = animationSpec
    )
    val offsetY by animateFloatAsState(
        targetValue = targetOffset.y.value,
        animationSpec = animationSpec
    )
    this.offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
}

/**
 * انیمیشن Scale با امکان تنظیم AnimationSpec سفارشی
 * @param targetScale مقدار نهایی مقیاس
 * @param durationMillis مدت زمان انیمیشن
 * @param delayMillis تاخیر انیمیشن
 * @param animationSpec تنظیمات سفارشی انیمیشن
 */
fun Modifier.scaleIn(
    targetScale: Float = 1f,
    durationMillis: Int = 1000,
    delayMillis: Int = 0,
    animationSpec: AnimationSpec<Float> = tween(durationMillis = durationMillis, delayMillis = delayMillis)
): Modifier = composed {
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = animationSpec
    )
    this.scale(scale)
}

/**
 * انیمیشن Shimmer با امکان تنظیم AnimationSpec سفارشی
 * @param durationMillis مدت زمان انیمیشن
 * @param delayMillis تاخیر انیمیشن
 * @param animationSpec تنظیمات سفارشی انیمیشن
 */
fun Modifier.shimmer(
    durationMillis: Int = 1500,
    delayMillis: Int = 0,
    animationSpec: AnimationSpec<Float> = tween(durationMillis = durationMillis, delayMillis = delayMillis)
): Modifier = composed {
    val translateX by animateFloatAsState(
        targetValue = 1f,
        animationSpec = animationSpec
    )

    val brush = Brush.linearGradient(
        listOf(Color.Gray.copy(alpha = 0.3f), Color.White, Color.Gray.copy(alpha = 0.3f)),
        start = Offset(-translateX * 300, 0f),
        end = Offset(translateX * 300, 0f)
    )

    this.background(brush)
}

/**
 * پیاده‌سازی پیشرفته برای `AnimationSpec` سفارشی
 * به عنوان مثال، استفاده از keyframes
 */
@Composable
fun customAnimationExample() {
    val customSpec = keyframes<Float> {
        durationMillis = 1000
        0f at 0
        1f at 500
        0.5f at 750
        1f at 1000
    }

    Box(
        modifier = Modifier
            .fadeIn(animationSpec = customSpec)
            .padding(16.dp)
    ) {
        Text("این یک انیمیشن سفارشی است!")
    }
}

/**
 * پیش‌نمایش از انیمیشن‌های ساخته‌شده
 */
@Preview
@Composable
fun PreviewAnimationExtensions() {
    Box(
        modifier = Modifier
            .fadeIn(targetAlpha = 1f, durationMillis = 1500)
            .padding(16.dp)
            .size(100.dp)
            .background(Color.Gray)
    ) {
        Text("Fade In", color = Color.White, modifier = Modifier.align(Alignment.Center))
    }
}
