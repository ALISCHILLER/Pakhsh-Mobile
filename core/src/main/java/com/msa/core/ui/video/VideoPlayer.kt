package com.msa.core.ui.video

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    volume: Float = 1f, // سطح صدا (پیش‌فرض: 1)
    playbackSpeed: Float = 1f, // سرعت پخش (پیش‌فرض: 1x)
    isMuted: Boolean = false, // آیا صدا خاموش شود؟
    initialPositionMs: Long = 0L, // موقعیت اولیه در ویدیو (به میلی‌ثانیه)
    controllerEnabled: Boolean = true // فعال/غیرفعال کردن کنترل‌ها
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // ایجاد و مدیریت ExoPlayerManager
    val exoPlayerManager = remember {
        ExoPlayerManager(context).apply {
            setVideo(videoUrl)
        }
    }

    // مدیریت Lifecycle
    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(exoPlayerManager)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(exoPlayerManager)
            exoPlayerManager.release()
        }
    }

    // تنظیمات صدا
    LaunchedEffect(volume, isMuted) {
        if (isMuted) {
            exoPlayerManager.mute()
        } else {
            exoPlayerManager.setVolume(volume)
        }
    }

    // تنظیمات سرعت پخش
    LaunchedEffect(playbackSpeed) {
        exoPlayerManager.setPlaybackSpeed(playbackSpeed)
    }

    // تنظیمات موقعیت اولیه
    LaunchedEffect(initialPositionMs) {
        exoPlayerManager.seekTo(initialPositionMs)
    }

    // نمایش ویدیو
    Box(modifier = modifier.fillMaxSize()) {
        StyledPlayerView(
            exoPlayer = exoPlayerManager.getExoPlayer(),
            controllerEnabled = controllerEnabled,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun StyledPlayerView(
    exoPlayer: ExoPlayer,
    controllerEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                player = exoPlayer
                useController = controllerEnabled // فعال/غیرفعال کردن کنترل‌ها
            }
        },
        modifier = modifier
    )
}

@Composable
@Preview
fun PlayerScreen() {
    val videoUrl = "https://cdn.pixabay.com/video/2015/08/20/468-136808389_large.mp4"

    VideoPlayer(
        videoUrl = videoUrl,
        modifier = Modifier.fillMaxSize()
    )
}
@Composable
@Preview
fun PlayerScreenWithCustomSettings() {
    val videoUrl = "https://cdn.pixabay.com/video/2015/08/20/468-136808389_large.mp4"

    Column {
        VideoPlayer(
            videoUrl = videoUrl,
            volume = 0.5f, // 50% صدا
            playbackSpeed = 1.5f, // سرعت پخش: 1.5x
            isMuted = false, // صدا روشن
            initialPositionMs = 5000L, // شروع از 5 ثانیه
            controllerEnabled = true, // فعال کردن کنترل‌ها
            modifier = Modifier.fillMaxSize()
        )
    }

}
