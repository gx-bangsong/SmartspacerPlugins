package com.kieronquinn.app.smartspacer.plugin.water.data

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface DrinkHistoryDao {
    @Insert
    suspend fun insert(drinkHistory: DrinkHistory)
}
