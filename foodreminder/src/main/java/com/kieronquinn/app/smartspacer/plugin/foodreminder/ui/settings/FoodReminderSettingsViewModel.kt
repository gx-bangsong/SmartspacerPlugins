package com.kieronquinn.app.smartspacer.plugin.foodreminder.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.foodreminder.data.FoodItem
import com.kieronquinn.app.smartspacer.plugin.foodreminder.data.FoodItemRepository
import com.kieronquinn.app.smartspacer.plugin.foodreminder.data.FoodReminderSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class FoodReminderSettingsViewModel(
    private val foodItemRepository: FoodItemRepository,
    private val settingsRepository: FoodReminderSettingsRepository
) : ViewModel() {

    val foodItems = foodItemRepository.getFoodItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val reminderLeadTimeDays = settingsRepository.reminderLeadTimeDays.asFlow()

    fun setReminderLeadTimeDays(days: Int) {
        viewModelScope.launch {
            settingsRepository.reminderLeadTimeDays.set(days)
        }
    }

    fun addFoodItem(name: String, storageMethod: String, expiryDate: Long) {
        viewModelScope.launch {
            foodItemRepository.addFoodItem(
                FoodItem(
                    name = name,
                    storageMethod = storageMethod,
                    expiryDate = expiryDate
                )
            )
        }
    }

    fun deleteFoodItem(item: FoodItem) {
        viewModelScope.launch {
            foodItemRepository.deleteFoodItem(item)
        }
    }
}