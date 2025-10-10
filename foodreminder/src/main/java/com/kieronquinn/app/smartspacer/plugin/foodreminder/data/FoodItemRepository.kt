package com.kieronquinn.app.smartspacer.plugin.foodreminder.data

import kotlinx.coroutines.flow.Flow

class FoodItemRepository(private val foodItemDao: FoodItemDao) {
    fun getFoodItems(): Flow<List<FoodItem>> = foodItemDao.getAll()

    suspend fun addFoodItem(item: FoodItem) {
        foodItemDao.insert(item)
    }

    suspend fun updateFoodItem(item: FoodItem) {
        foodItemDao.update(item)
    }

    suspend fun deleteFoodItem(item: FoodItem) {
        foodItemDao.delete(item)
    }
}