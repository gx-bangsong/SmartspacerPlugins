package com.kieronquinn.app.smartspacer.plugin.waterreminder.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.waterreminder.data.WaterReminderSettingsRepository
import kotlinx.coroutines.launch

class WaterReminderSettingsViewModel(
    private val settingsRepository: WaterReminderSettingsRepository
) : ViewModel() {

    val dailyGoalMl = settingsRepository.dailyGoalMl.asFlow()
    val cupSizeMl = settingsRepository.cupSizeMl.asFlow()
    val activeHourStart = settingsRepository.activeHourStart.asFlow()
    val activeHourEnd = settingsRepository.activeHourEnd.asFlow()
    val spacerStyle = settingsRepository.spacerStyle.asFlow()
    val currentProgressCups = settingsRepository.currentProgressCups.asFlow()

    fun setDailyGoal(goal: Int) {
        viewModelScope.launch {
            settingsRepository.dailyGoalMl.set(goal)
        }
    }

    fun setCupSize(size: Int) {
        viewModelScope.launch {
            settingsRepository.cupSizeMl.set(size)
        }
    }

    fun setActiveHours(start: Int, end: Int) {
        viewModelScope.launch {
            settingsRepository.activeHourStart.set(start)
            settingsRepository.activeHourEnd.set(end)
        }
    }

    fun setSpacerStyle(style: Int) {
        viewModelScope.launch {
            settingsRepository.spacerStyle.set(style)
        }
    }

    fun adjustProgress(amount: Int) {
        viewModelScope.launch {
            val current = settingsRepository.currentProgressCups.get()
            val newProgress = (current + amount).coerceAtLeast(0)
            settingsRepository.currentProgressCups.set(newProgress)
        }
    }
}