package com.msa.core.ui.video


import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

/**
 * مدیریت‌کننده ExoPlayer برای Jetpack Compose.
 */
class ExoPlayerManager(private val context: Context) : LifecycleEventObserver {

    private val exoPlayer = ExoPlayer.Builder(context).build().apply {
        repeatMode = ExoPlayer.REPEAT_MODE_ALL
        playWhenReady = true
    }

    /**
     * تنظیم ویدیو برای پخش.
     */
    fun setVideo(videoUrl: String) {
        val mediaItem = MediaItem.fromUri(videoUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    /**
     * شروع پخش ویدیو.
     */
    fun play() {
        exoPlayer.play()
    }

    /**
     * متوقف کردن پخش ویدیو.
     */
    fun pause() {
        exoPlayer.pause()
    }

    /**
     * رها کردن منابع ExoPlayer.
     */
    fun release() {
        exoPlayer.release()
    }

    /**
     * تنظیم سطح صدا.
     */
    fun setVolume(volume: Float) {
        exoPlayer.volume = volume.coerceIn(0f, 1f) // مقدار صدا بین 0 تا 1
    }

    /**
     * تنظیم سرعت پخش.
     */
    fun setPlaybackSpeed(speed: Float) {
        exoPlayer.setPlaybackSpeed(speed.coerceIn(0.5f, 2f)) // محدوده سرعت پخش: 0.5x تا 2x
    }

    /**
     * جلوگیری از پخش صدا.
     */
    fun mute() {
        exoPlayer.volume = 0f
    }

    /**
     * فعال کردن صدا.
     */
    fun unMute() {
        exoPlayer.volume = 1f
    }

    /**
     * پرش به زمان خاص در ویدیو.
     */
    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs.coerceAtLeast(0))
    }

    /**
     * دریافت ExoPlayer.
     */
    fun getExoPlayer(): ExoPlayer {
        return exoPlayer
    }

    /**
     * مدیریت Lifecycle.
     */

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
            androidx.lifecycle.Lifecycle.Event.ON_RESUME -> exoPlayer.play()
            androidx.lifecycle.Lifecycle.Event.ON_DESTROY -> exoPlayer.release()
            else -> Unit
        }
    }
}