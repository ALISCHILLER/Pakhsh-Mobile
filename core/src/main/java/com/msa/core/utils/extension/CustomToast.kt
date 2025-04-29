package com.msa.core.utils.extension



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit


/**
 * نمایش یک نمونه مشابه به Toast با استفاده از Jetpack Compose
 */
@Composable
fun CustomToast(
    message: String,
    backgroundColor: Color = Color.Black,
    textColor: Color = Color.White,
    textSize: TextUnit = 14.sp,
    cornerRadius: Dp = 16.dp
) {
    Box(
        modifier = Modifier
            .background(color = backgroundColor, shape = RoundedCornerShape(cornerRadius))
            .fillMaxWidth()
    ) {
        Text(
            text = message,
            color = textColor,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = textSize
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * پیش‌نمایش از Toast سفارشی در Jetpack Compose
 */
@Preview(showBackground = true)
@Composable
fun CustomToastPreview() {
    CustomToast(
        message = "این یک Toast سفارشی است!",
        backgroundColor = Color.Black,
        textColor = Color.White,
        textSize = 16.sp,
        cornerRadius = 12.dp
    )
}
