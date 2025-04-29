package com.msa.core.utils.extension


import android.os.Bundle
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * اکستنشن برای حذف آیتم‌ها با کشیدن
 */
@Composable
fun <T> SwipeToDismissItem(
    item: T,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
//    val swipeableState = rememberSwipeableState(initialValue = 0)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount > 100f) {
                        onDismiss() // حذف آیتم
                    }
                }
            }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            content(item)
        }
        IconButton(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(8.dp),
            onClick = { onDismiss() }
        ) {
            Icon(imageVector =   Icons.Filled.Clear, contentDescription = "Delete")
        }
    }
}

@Preview
@Composable
fun PreviewSwipeToDismissItem() {
    val item = "Item to swipe"
    SwipeToDismissItem(item = item, onDismiss = { /* handle dismiss */ }) { item ->
        Text(text = item)
    }
}
