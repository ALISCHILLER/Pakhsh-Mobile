package com.msa.core.utils.extension

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible

/**
 * توابع Extension برای مدیریت ویوها (View)
 * این فایل توابع کمکی رایج برای ویوها را فراهم می‌کند.
 */

/**
 * نمایش ویو
 */
fun View.show() {
    this.visibility = View.VISIBLE
}

/**
 * مخفی کردن ویو (حفظ فضا)
 */
fun View.hide() {
    this.visibility = View.GONE
}

/**
 * نامرئی کردن ویو (فضای ویو حفظ می‌شود)
 */
fun View.invisible() {
    this.visibility = View.INVISIBLE
}

/**
 * تغییر وضعیت ویو بر اساس شرط
 * @param shouldShow اگر true باشد ویو نمایش داده می‌شود، در غیر اینصورت مخفی می‌شود
 */
fun View.setVisible(shouldShow: Boolean) {
    this.visibility = if (shouldShow) View.VISIBLE else View.GONE
}

/**
 * بررسی اینکه ویو قابل مشاهده است یا خیر
 * @return true اگر ویو قابل مشاهده باشد، false در غیر اینصورت
 */
fun View.isVisible(): Boolean {
    return this.visibility == View.VISIBLE
}

/**
 * فعال یا غیرفعال کردن یک ویو به همراه تغییر شفافیت (alpha)
 * @param enabled وضعیت فعال یا غیرفعال
 */
fun View.setEnabledWithAlpha(enabled: Boolean) {
    this.isEnabled = enabled
    this.alpha = if (enabled) 1.0f else 0.5f
}

/**
 * حذف ویو از والد خود
 */
fun View.removeFromParent() {
    (this.parent as? ViewGroup)?.removeView(this)
}

/**
 * کلیک کردن سریع جلوگیری از کلیک‌های پشت سرهم (Debounce Click)
 * @param interval فاصله زمانی بین کلیک‌ها (بر حسب میلی‌ثانیه)
 * @param action عملی که باید هنگام کلیک انجام شود
 */
fun View.setDebouncedClickListener(interval: Long = 600L, action: (View) -> Unit) {
    var lastClickTime = 0L

    this.setOnClickListener { view ->
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > interval) {
            lastClickTime = currentTime
            action(view)
        }
    }
}