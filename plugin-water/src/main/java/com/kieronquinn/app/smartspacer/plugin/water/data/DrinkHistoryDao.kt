package com.kieronquinn.app.smartspacer.plugin.water.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DrinkHistoryDao {
    @Insert
    suspend fun insert(drinkHistory: DrinkHistory)

    @Query("SELECT * FROM drink_history WHERE timestamp BETWEEN :startOfDay AND :endOfDay")
    suspend fun getDrinksForDate(startOfDay: Long, endOfDay: Long): List<DrinkHistory>
}
