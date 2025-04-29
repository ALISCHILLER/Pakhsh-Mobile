package com.msa.core.utils.extension

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

/**
 * نمایش LazyColumn با انیمیشن FadeIn برای آیتم‌ها
 */
@Composable
fun <T> AnimatedLazyColumn(
    items: List<T>,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    LazyColumn(modifier = modifier) {
        items(items) { item ->
            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .fadeIn() // انیمیشن FadeIn
            ) {
                content(item)
            }
        }
    }
}

/**
 * نمایش LazyRow با انیمیشن SlideIn برای آیتم‌ها
 */
@Composable
fun <T> AnimatedLazyRow(
    items: List<T>,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    LazyRow(modifier = modifier) {
        items(items) { item ->
            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .slideIn() // انیمیشن SlideIn
            ) {
                content(item)
            }
        }
    }
}

@Preview
@Composable
fun PreviewAnimatedLazyColumn() {
    val items = listOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5")
    AnimatedLazyColumn(items = items) { item ->
        Text(text = item, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview
@Composable
fun PreviewAnimatedLazyRow() {
    val items = listOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5")
    AnimatedLazyRow(items = items) { item ->
        Text(text = item, style = MaterialTheme.typography.bodyLarge)
    }
}
