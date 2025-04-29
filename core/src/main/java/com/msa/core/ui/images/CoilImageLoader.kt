package com.msa.core.ui.images

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import com.msa.core.R
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.ui.graphics.painter.Painter
import coil3.compose.AsyncImagePainter


/**
 * کامپوننتی برای بارگذاری تصاویر با استفاده از Coil.
 */
@Composable
fun CoilImageLoader(
    imageUrl: String? = null, // URL تصویر
    resourceId: Int? = null, // Resource ID تصویر محلی
    stateFlow: StateFlow<AsyncImagePainter.State>? = null, // StateFlow برای مدیریت وضعیت
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    placeholder: Painter? = null, // تصویر جایگزین قبل از بارگذاری
    error: Painter? = null, // تصویر جایگزین در صورت خطا
    contentScale: ContentScale = ContentScale.Crop, // مقیاس محتوا
    size: Dp? = null // اندازه پیش‌فرض تصویر
) {
    // تعیین منبع تصویر (URL یا Resource ID)
    val imageModel = when {
        imageUrl != null -> ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .size(Size.ORIGINAL)
            .crossfade(true) // انتقال نرم بین تصویر پیش‌فرض و تصویر اصلی
            .build()
        resourceId != null -> ImageRequest.Builder(LocalContext.current)
            .data(resourceId)
            .size(Size.ORIGINAL)
            .crossfade(true)
            .build()
        else -> null
    }

    // بارگذاری تصویر با استفاده از Coil
    val painter = rememberAsyncImagePainter(
        model = imageModel,
        placeholder = placeholder,
        error = error
    )

    // دریافت آخرین مقدار از StateFlow (اگر وجود داشته باشد)
    val currentState = stateFlow?.collectAsState()?.value ?: painter.state

    Box(
        modifier = modifier
            .then(size?.let { Modifier.size(it) } ?: Modifier.size(100.dp)), // استفاده از اندازه ورودی یا پیش‌فرض
        contentAlignment = Alignment.Center
    ) {
        when (currentState) {
            is AsyncImagePainter.State.Loading -> {
                // نمایش وضعیت بارگذاری با پروگرس بار
                LoadingState(placeholder = placeholder, contentScale = contentScale)
            }
            is AsyncImagePainter.State.Error -> {
                // نمایش وضعیت خطا با تصویر خطا
                ErrorState(error = error, contentScale = contentScale)
            }
            else -> {
                // نمایش تصویر پس از بارگذاری یا نمایش پیش‌فرض
                Image(
                    painter = painter,
                    contentDescription = contentDescription,
                    contentScale = contentScale,
                    modifier = Modifier.matchParentSize()
                )
            }
        }
    }
}

// وضعیت بارگذاری
@Composable
fun LoadingState(placeholder: Painter?, contentScale: ContentScale) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        placeholder?.let {
            Image(
                painter = it,
                contentDescription = "Loading",
                contentScale = contentScale,
                modifier = Modifier.matchParentSize()
            )
        }
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

// وضعیت خطا
@Composable
fun ErrorState(error: Painter?, contentScale: ContentScale) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        error?.let {
            Image(
                painter = it,
                contentDescription = "Error",
                contentScale = contentScale,
                modifier = Modifier.matchParentSize()
            )
        }
        Text("Error loading image", modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
@Preview
fun ImageListPreview() {
    val imageUrls = listOf("https://example.com/image1.jpg", "https://example.com/image2.jpg")
    ImageList(imageUrls = imageUrls)
}

@Composable
fun ImageList(imageUrls: List<String>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(imageUrls) { imageUrl ->
            CoilImageLoader(
                imageUrl = imageUrl,
                contentDescription = "Image from URL",
                placeholder = painterResource(id = R.drawable.placeholder),
                error = painterResource(id = R.drawable.error_image),
                size = 120.dp
            )
        }
    }
}

@Composable
@Preview
fun ImageViewerPreview() {
    val imageUrl = "https://example.com/sample-image.jpg"
    CoilImageLoader(
        imageUrl = imageUrl,
        contentDescription = "Sample Image",
        placeholder = painterResource(id = R.drawable.placeholder),
        error = painterResource(id = R.drawable.error_image),
        size = 200.dp
    )
}

@Composable
fun ImageViewerWithStateFlowPreview() {
    val imageStateFlow = remember { MutableStateFlow<AsyncImagePainter.State>(AsyncImagePainter.State.Empty) }

    CoilImageLoader(
        imageUrl = "https://example.com/image.jpg",
        stateFlow = imageStateFlow, // ارسال StateFlow برای مدیریت وضعیت بارگذاری
        modifier = Modifier.size(250.dp),
        contentDescription = "Image with StateFlow",
        placeholder = painterResource(id = R.drawable.placeholder),
        error = painterResource(id = R.drawable.error_image),
        contentScale = ContentScale.Crop
    )
}
