package com.msa.core.utils.validation




/**
 * کلاس اعتبارسنجی شماره موبایل.
 */
object MobileNumberValidator {

    // پیش‌شماره‌های معتبر
    private val validPrefixes = listOf(
        "0912", "0919", "0935", "0936", "0937", "0938", "0939", "0901", "0902", "0903",
        "0904", "0905", "0920", "0921", "0922", "0930", "0933", "0934", "0935", "0990"
    )

    /**
     * اعتبارسنجی شماره موبایل.
     * @param mobileNumber شماره موبایل ورودی.
     * @return true اگر شماره موبایل معتبر باشد، در غیر این صورت false.
     */
    fun isValid(mobileNumber: String): Boolean {
        // بررسی طول شماره موبایل و اینکه همه کاراکترها عدد باشند
        if (mobileNumber.length != 11 || !mobileNumber.all { it.isDigit() }) return false

        // بررسی پیش‌شماره
        return mobileNumber.take(4) in validPrefixes
    }

    /**
     * اعتبارسنجی شماره موبایل و بازگرداندن پیام خطا.
     * @param mobileNumber شماره موبایل ورودی.
     * @return پیام خطا در صورت نامعتبر بودن، در غیر این صورت null.
     */
    fun validateAndGetErrorMessage(mobileNumber: String): String? {
        // بررسی طول شماره موبایل
        if (mobileNumber.length != 11) {
            return "طول شماره موبایل باید ۱۱ رقم باشد."
        }

        // بررسی اینکه همه کاراکترها عدد باشند
        if (!mobileNumber.all { it.isDigit() }) {
            return "شماره موبایل فقط باید شامل اعداد باشد."
        }

        // بررسی پیش‌شماره
        if (mobileNumber.take(4) !in validPrefixes) {
            return "پیش‌شماره نامعتبر است."
        }

        return null
    }
}



//val mobileNumber = "09123456789" // مثال شماره موبایل معتبر
//
//if (MobileNumberValidator.isValid(mobileNumber)) {
//    println("شماره موبایل معتبر است.")
//} else {
//    println("شماره موبایل نامعتبر است.")
//}

//var mobileNumber by remember { mutableStateOf("") }
//var errorMessage by remember { mutableStateOf<String?>(null) }
//errorMessage = MobileNumberValidator.validateAndGetErrorMessage(mobileNumber)
//if (errorMessage != null) {
//    Text(text = errorMessage!!, color = androidx.compose.ui.graphics.Color.Red)
//} else {
//    Text(text = "شماره موبایل معتبر است.", color = androidx.compose.ui.graphics.Color.Green)
//}