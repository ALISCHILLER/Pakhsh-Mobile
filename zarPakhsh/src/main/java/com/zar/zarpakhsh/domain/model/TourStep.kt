package com.zar.zarpakhsh.domain.model

enum class TourStep(
    val stepName: String,
    val apiCount: Int = 1
) {
    FETCH_CUSTOMERS("بارگزاری مشتریان"),
    FETCH_PRODUCTS("بارگزاری محصولات", 2), // فرض بر اینکه 2 درخواست برای محصولات لازم است
    FETCH_SETTINGS("بارگزاری تنظیمات"),
    FETCH_LOCATIONS("بارگزاری موقعیت‌ها"),
    FETCH_REPORTS("بارگزاری گزارش‌ها", 3), // فرض بر اینکه 3 درخواست برای گزارش‌ها لازم است
    FETCH_TOUR_DATA("بارگزاری اطلاعات تور");

    // نمایش خلاصه‌ای از مراحل
    companion object {
        fun getStepsSummary(): String {
            return values().joinToString("\n") { "${it.stepName} (API Count: ${it.apiCount})" }
        }
    }
}