package com.msa.visitApp.core.data.local.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.msa.visitApp.core.data.local.entities.VisitEntity

@Dao
interface VisitDao {
    @Insert
    suspend fun insert(visit: VisitEntity)

    @Query("SELECT * FROM visit_table")
    suspend fun getAllVisits(): List<VisitEntity>
}