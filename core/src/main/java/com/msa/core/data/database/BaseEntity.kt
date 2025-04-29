package com.msa.core.data.database

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

/**
 * کلاس پایه برای Entity‌ها.
 */
open class BaseEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") open var id: Long = 0
)