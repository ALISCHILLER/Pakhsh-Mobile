package com.zar.zarvisitapp.core.data.local.entities


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "visit_table")
data class VisitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val visitorName: String,
    val date: String
)
