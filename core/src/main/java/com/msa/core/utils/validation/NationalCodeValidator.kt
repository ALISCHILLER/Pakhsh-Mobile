package com.msa.core.utils.validation




/**
 * کلاس اعتبارسنجی کد ملی.
 * این کلاس از الگوریتم استاندارد ایران برای اعتبارسنجی کد ملی استفاده می‌کند.
 */
object NationalCodeValidator {

    /**
     * اعتبارسنجی کد ملی بر اساس استانداردهای رسمی ایران.
     * @param nationalCode کد ملی ورودی
     * @return true اگر کد ملی معتبر باشد، در غیر این صورت false
     */
    fun isValid(nationalCode: String): Boolean {
        // بررسی طول کد ملی
        if (nationalCode.length != 10) return false

        // بررسی اینکه تمام کاراکترها عدد باشند
        if (!nationalCode.all { it.isDigit() }) return false

        // بررسی کد ملی‌های نامعتبر مانند "0000000000"
        if (nationalCode.toSet().size == 1) return false

        // محاسبه رقم کنترل
        val digits = nationalCode.map { it.toString().toInt() }
        val checkDigit = digits.last()
        val sum = digits.take(9).foldIndexed(0) { index, acc, digit ->
            acc + digit * (10 - index)
        }

        val remainder = sum % 11
        val expectedCheckDigit = if (remainder < 2) remainder else 11 - remainder

        return checkDigit == expectedCheckDigit
    }

    /**
     * اعتبارسنجی کد ملی و بازگرداندن پیام خطا.
     * در صورت نامعتبر بودن، پیام خطا را برمی‌گرداند.
     * @param nationalCode کد ملی ورودی
     * @return پیام خطا در صورت نامعتبر بودن، در غیر این صورت null
     */
    fun validateAndGetErrorMessage(nationalCode: String): String? {
        // بررسی طول کد ملی
        if (nationalCode.length != 10) {
            return "طول کد ملی باید ۱۰ رقم باشد."
        }

        // بررسی اینکه تمام کاراکترها عدد باشند
        if (!nationalCode.all { it.isDigit() }) {
            return "کد ملی باید فقط شامل اعداد باشد."
        }

        // بررسی کد ملی‌های تکراری
        if (nationalCode.toSet().size == 1) {
            return "کد ملی نمی‌تواند شامل ارقام تکراری باشد."
        }

        // بررسی اعتبار کد ملی
        if (!isValid(nationalCode)) {
            return "کد ملی نامعتبر است."
        }

        return null
    }
}



// نحوه استفاده
//val nationalCode = "0079021847" // مثال کد ملی معتبر
//
//if (NationalCodeValidator.isValid(nationalCode)) {
//    println("کد ملی معتبر است.")
//} else {
//    println("کد ملی نامعتبر است.")
//}

//var nationalCode by remember { mutableStateOf("") }
//var errorMessage by remember { mutableStateOf<String?>(null) }
//errorMessage = NationalCodeValidator.validateAndGetErrorMessage(nationalCode)
//
//if (errorMessage != null) {
//    Text(text = errorMessage!!, color = androidx.compose.ui.graphics.Color.Red)
//} else {
//    Text(text = "کد ملی معتبر است.", color = androidx.compose.ui.graphics.Color.Green)
//}