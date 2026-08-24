package com.kieronquinn.app.smartspacer.plugin.medication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DoseHistoryDao {
    @Insert
    suspend fun insert(doseHistory: DoseHistory)

    @Query("SELECT * FROM dose_history WHERE medicationId = :medicationId AND timestamp >= :start AND timestamp < :end")
    suspend fun getForMedicationBetween(medicationId: Int, start: Long, end: Long): List<DoseHistory>
}
