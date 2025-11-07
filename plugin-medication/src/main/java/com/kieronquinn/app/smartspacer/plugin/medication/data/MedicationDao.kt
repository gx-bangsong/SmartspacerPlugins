package com.kieronquinn.app.smartspacer.plugin.medication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medication: Medication)

    @Update
    suspend fun update(medication: Medication)

    @Delete
    suspend fun delete(medication: Medication)

    @Query("SELECT * FROM medications ORDER BY nextDoseTs ASC")
    fun getAll(): Flow<List<Medication>>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getById(id: Int): Medication?

}