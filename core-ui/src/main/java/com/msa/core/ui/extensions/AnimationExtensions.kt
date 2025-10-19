package com.msa.core.ui.extensions

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

/**
 * مجموعه‌ای از اکستنشن‌های انیمیشن برای Modifier و الگوهای رایج Compose.
 */

/**
 * انیمیشن محو شدن تدریجی برای Modifier ها.
 */
fun Modifier.fadeIn(
    targetAlpha: Float = 1f,
    durationMillis: Int = 350,
    delayMillis: Int = 0,
    animationSpec: AnimationSpec<Float> = tween(durationMillis = durationMillis, delayMillis = delayMillis)
): Modifier = composed {
    val alpha by animateFloatAsState(targetValue = targetAlpha, animationSpec = animationSpec, label = "fadeIn")
    this.graphicsLayer(alpha = alpha)
}

/**
 * انیمیشن جابه‌جایی تدریجی با پشتیبانی صحیح از Density.
 */
fun Modifier.slideIn(
    targetOffset: DpOffset = DpOffset.Zero,
    durationMillis: Int = 300,
    delayMillis: Int = 0,
    animationSpec: AnimationSpec<Float> = tween(durationMillis = durationMillis, delayMillis = delayMillis)
): Modifier = composed {
    val density = LocalDensity.current
    val targetOffsetX = remember(targetOffset, density) { with(density) { targetOffset.x.toPx() } }
    val targetOffsetY = remember(targetOffset, density) { with(density) { targetOffset.y.toPx() } }

    val animatedX by animateFloatAsState(targetValue = targetOffsetX, animationSpec = animationSpec, label = "slideInX")
    val animatedY by animateFloatAsState(targetValue = targetOffsetY, animationSpec = animationSpec, label = "slideInY")

    this.offset { IntOffset(animatedX.roundToInt(), animatedY.roundToInt()) }
}

/**
 * انیمیشن Scale ساده برای Modifier.
 */
fun Modifier.scaleIn(
    targetScale: Float = 1f,
    durationMillis: Int = 300,
    delayMillis: Int = 0,
    animationSpec: AnimationSpec<Float> = tween(durationMillis = durationMillis, delayMillis = delayMillis)
): Modifier = composed {
    val scale by animateFloatAsState(targetValue = targetScale, animationSpec = animationSpec, label = "scaleIn")
    this.graphicsLayer(scaleX = scale, scaleY = scale)
}

/**
 * افکت Shimmer سبک برای جای‌نماها.
 */
fun Modifier.shimmer(
    durationMillis: Int = 1500,
    delayMillis: Int = 0,
    animationSpec: AnimationSpec<Float> = tween(durationMillis = durationMillis, delayMillis = delayMillis)
): Modifier = composed {
    val progress by animateFloatAsState(targetValue = 1f, animationSpec = animationSpec, label = "shimmer")
    val translateX = remember(progress) { progress * 300f }

    val brush = Brush.linearGradient(
        colors = listOf(Color.Gray.copy(alpha = 0.3f), Color.White, Color.Gray.copy(alpha = 0.3f)),
        start = Offset(-translateX, 0f),
        end = Offset(translateX, 0f)
    )
    this.background(brush)
}

/**
 * جهت نمایش لیست متحرک.
 */
enum class LazyListOrientation { Vertical, Horizontal }

/**
 * لیست Lazy با انیمیشن‌های آماده برای ورود آیتم‌ها.
 */
@Composable
fun <T> AnimatedLazyList(
    items: List<T>,
    orientation: LazyListOrientation = LazyListOrientation.Vertical,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    key: ((item: T) -> Any)? = null,
    itemContent: @Composable (T) -> Unit
) {
    val fillMaxWidth = orientation == LazyListOrientation.Vertical
    val listContent: LazyListScope.() -> Unit = {
        val builder: LazyListScope.(List<T>) -> Unit = { data ->
            if (key != null) {
                items(data, key = key) { item -> AnimatedListItem(fillMaxWidth = fillMaxWidth) { itemContent(item) } }
            } else {
                items(data) { item -> AnimatedListItem(fillMaxWidth = fillMaxWidth) { itemContent(item) } }
            }
        }
        builder(items)
    }

    when (orientation) {
        LazyListOrientation.Vertical -> LazyColumn(modifier = modifier, contentPadding = contentPadding, content = listContent)
        LazyListOrientation.Horizontal -> LazyRow(modifier = modifier, contentPadding = contentPadding, content = listContent)
    }
}

@Composable
private fun AnimatedListItem(
    modifier: Modifier = Modifier,
    fillMaxWidth: Boolean,
    itemContent: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .then(if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier)
            .padding(vertical = 4.dp)
            .fadeIn()
            .slideIn(targetOffset = DpOffset(0.dp, 12.dp))
    ) {
        val cardModifier = if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier
        Card(modifier = cardModifier) {
            Box(modifier = Modifier.padding(16.dp)) {
                itemContent()
            }
        }
    }
}

@Preview
@Composable
private fun AnimatedLazyListPreview() {
    val sampleItems = remember { (1..5).map { "آیتم $it" } }
    AnimatedLazyList(items = sampleItems) { item ->
        Text(text = item, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp))
    }
}

@Preview
@Composable
private fun ShimmerPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shimmer()
    ) {
        Text(text = "Loading…", modifier = Modifier.align(Alignment.Center))
    }
}