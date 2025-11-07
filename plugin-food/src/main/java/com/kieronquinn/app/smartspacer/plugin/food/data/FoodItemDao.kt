package com.kieronquinn.app.smartspacer.plugin.food.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(foodItem: FoodItem)

    @Update
    suspend fun update(foodItem: FoodItem)

    @Delete
    suspend fun delete(foodItem: FoodItem)

    @Query("SELECT * FROM food_items ORDER BY expiryDate ASC")
    fun getAll(): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE id = :id")
    suspend fun getById(id: Int): FoodItem?

}