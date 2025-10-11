package com.kieronquinn.app.smartspacer.plugin.medicationreminder.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.model.DoseLog
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.model.Medication
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Transaction
    @Query("SELECT * FROM medications")
    fun getMedications(): Flow<List<Medication>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication)

    @Query("DELETE FROM medications WHERE id = :medicationId")
    suspend fun deleteMedication(medicationId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun logDose(doseLog: DoseLog)

    @Query("SELECT * FROM dose_logs WHERE medicationId = :medicationId ORDER BY doseTimestamp DESC")
    fun getDoseLogsForMedication(medicationId: Int): Flow<List<DoseLog>>
}