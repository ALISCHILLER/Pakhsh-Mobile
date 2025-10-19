package com.msa.core.ui.legacy

import android.view.View
import android.view.ViewGroup

/**
 * اکستنشن‌های کاربردی برای View های کلاسیک.
 */

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.setVisible(shouldShow: Boolean) {
    visibility = if (shouldShow) View.VISIBLE else View.GONE
}

fun View.isVisible(): Boolean = visibility == View.VISIBLE

fun View.setEnabledWithAlpha(enabled: Boolean) {
    isEnabled = enabled
    alpha = if (enabled) 1.0f else 0.5f
}

fun View.removeFromParent() {
    (parent as? ViewGroup)?.removeView(this)
}

fun View.setDebouncedClickListener(interval: Long = 600L, action: (View) -> Unit) {
    var lastClickTime = 0L
    setOnClickListener { view ->
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > interval) {
            lastClickTime = currentTime
            action(view)
        }
    }
}