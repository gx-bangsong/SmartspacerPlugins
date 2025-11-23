package com.kieronquinn.app.smartspacer.plugin.medication.data

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface DoseHistoryDao {
    @Insert
    suspend fun insert(doseHistory: DoseHistory)
}
