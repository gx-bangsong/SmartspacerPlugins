package com.kieronquinn.app.smartspacer.plugin.foodreminder

import androidx.room.Room
import com.kieronquinn.app.smartspacer.plugin.foodreminder.data.FoodItemDatabase
import com.kieronquinn.app.smartspacer.plugin.foodreminder.data.FoodItemRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val foodReminderModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            FoodItemDatabase::class.java, "food_item_database"
        ).build()
    }
    single { get<FoodItemDatabase>().foodItemDao() }
    single { FoodItemRepository(get()) }
    single { FoodReminderSettings(androidContext()) }
}