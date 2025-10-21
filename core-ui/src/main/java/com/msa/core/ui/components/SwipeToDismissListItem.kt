package com.msa.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissState
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * آیتم قابل حذف با جسچر Swipe با تکیه بر SwipeToDismiss متریال.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> SwipeToDismissListItem(
    item: T,
    onDismiss: (T) -> Unit,
    modifier: Modifier = Modifier,
    directions: Set<DismissDirection> = setOf(DismissDirection.EndToStart),
    thresholdFraction: Float = 0.35f,
    background: @Composable (DismissState) -> Unit = { DefaultSwipeBackground(it) },
    content: @Composable (T) -> Unit
) {
    val dismissState = rememberDismissState(confirmValueChange = { value ->
        if (value == DismissValue.DismissedToEnd || value == DismissValue.DismissedToStart) {
            onDismiss(item)
            true
        } else {
            false
        }
    })

    SwipeToDismiss(
        state = dismissState,
        background = { background(dismissState) },
        directions = directions,
        dismissThresholds = { FractionalThreshold(thresholdFraction) },
        dismissContent = {
            Card(modifier = Modifier.fillMaxWidth()) {
                content(item)
            }
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun DefaultSwipeBackground(state: DismissState) {
    val targetColor = when (state.targetValue) {
        DismissValue.DismissedToStart, DismissValue.DismissedToEnd -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when (state.targetValue) {
        DismissValue.DismissedToStart, DismissValue.DismissedToEnd -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val alignment = when (state.dismissDirection) {
        DismissDirection.StartToEnd -> Alignment.CenterStart
        DismissDirection.EndToStart -> Alignment.CenterEnd
        null -> Alignment.CenterEnd
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(targetColor)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = alignment
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = contentColor
            )
            Text(text = "حذف", color = contentColor)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
private fun SwipeToDismissListItemPreview() {
    SwipeToDismissListItem(item = "Sample", onDismiss = {}) { value ->
        Text(text = value, modifier = Modifier.padding(24.dp))
    }
}