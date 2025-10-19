package com.msa.core.logging

import timber.log.Timber

class FileLoggingTree(private val helper: LoggerHelper) : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        helper.log(levelForPriority(priority), tag, message, t)
    }

    private fun levelForPriority(priority: Int): String = when (priority) {
        7 -> "ASSERT"
        6 -> "ERROR"
        5 -> "WARN"
        4 -> "INFO"
        3 -> "DEBUG"
        else -> "TRACE"
    }
}