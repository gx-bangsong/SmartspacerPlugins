package com.kieronquinn.app.smartspacer.plugin.medicationreminder.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TakenDoseDao {
    @Query("SELECT * FROM taken_doses WHERE medicationId = :medicationId")
    fun getTakenDosesForMedication(medicationId: Int): Flow<List<TakenDose>>

    @Insert
    suspend fun insert(takenDose: TakenDose)
}