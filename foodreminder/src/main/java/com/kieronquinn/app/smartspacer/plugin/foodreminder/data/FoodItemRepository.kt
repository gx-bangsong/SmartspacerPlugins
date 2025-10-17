package com.kieronquinn.app.smartspacer.plugin.foodreminder.data

import kotlinx.coroutines.flow.Flow

class FoodItemRepository(private val foodItemDao: FoodItemDao) {
    val allFoodItems: Flow<List<FoodItem>> = foodItemDao.getAll()

    suspend fun insert(foodItem: FoodItem) {
        foodItemDao.insert(foodItem)
    }

    suspend fun update(foodItem: FoodItem) {
        foodItemDao.update(foodItem)
    }

    suspend fun deleteById(id: Int) {
        foodItemDao.deleteById(id)
    }
}