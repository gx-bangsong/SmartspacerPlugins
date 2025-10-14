package com.kieronquinn.app.smartspacer.plugin.water.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.water.repositories.DisplayMode
import com.kieronquinn.app.smartspacer.plugin.water.repositories.WaterDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface WaterSettingsViewModel {
    val dailyGoalMl: StateFlow<Int>
    val cupMl: StateFlow<Int>
    val activeStartMinutes: StateFlow<Int>
    val activeEndMinutes: StateFlow<Int>
    val displayMode: StateFlow<DisplayMode>
    val resetAtActiveStart: StateFlow<Boolean>
    val smartAdjust: StateFlow<Boolean>
    val snoozeMinutes: StateFlow<Int>

    fun setDailyGoalMl(value: Int)
    fun setCupMl(value: Int)
    fun setActiveStartMinutes(value: Int)
    fun setActiveEndMinutes(value: Int)
    fun setDisplayMode(value: DisplayMode)
    fun setResetAtActiveStart(value: Boolean)
    fun setSmartAdjust(value: Boolean)
    fun setSnoozeMinutes(value: Int)

    // Debug functions
    fun simulateReminder()
    fun clearTodaySchedule()
}

class WaterSettingsViewModelImpl(
    private val waterDataRepository: WaterDataRepository
) : ViewModel(), WaterSettingsViewModel {

    private val _dailyGoalMl = MutableStateFlow(waterDataRepository.dailyGoalMl)
    override val dailyGoalMl: StateFlow<Int> = _dailyGoalMl.asStateFlow()

    private val _cupMl = MutableStateFlow(waterDataRepository.cupMl)
    override val cupMl: StateFlow<Int> = _cupMl.asStateFlow()

    private val _activeStartMinutes = MutableStateFlow(waterDataRepository.activeStartMinutes)
    override val activeStartMinutes: StateFlow<Int> = _activeStartMinutes.asStateFlow()

    private val _activeEndMinutes = MutableStateFlow(waterDataRepository.activeEndMinutes)
    override val activeEndMinutes: StateFlow<Int> = _activeEndMinutes.asStateFlow()

    private val _displayMode = MutableStateFlow(waterDataRepository.displayMode)
    override val displayMode: StateFlow<DisplayMode> = _displayMode.asStateFlow()

    private val _resetAtActiveStart = MutableStateFlow(waterDataRepository.resetAtActiveStart)
    override val resetAtActiveStart: StateFlow<Boolean> = _resetAtActiveStart.asStateFlow()

    private val _smartAdjust = MutableStateFlow(waterDataRepository.smartAdjust)
    override val smartAdjust: StateFlow<Boolean> = _smartAdjust.asStateFlow()

    private val _snoozeMinutes = MutableStateFlow(waterDataRepository.snoozeMinutes)
    override val snoozeMinutes: StateFlow<Int> = _snoozeMinutes.asStateFlow()

    override fun setDailyGoalMl(value: Int) {
        viewModelScope.launch {
            waterDataRepository.dailyGoalMl = value
            _dailyGoalMl.value = value
        }
    }

    override fun setCupMl(value: Int) {
        viewModelScope.launch {
            waterDataRepository.cupMl = value
            _cupMl.value = value
        }
    }

    override fun setActiveStartMinutes(value: Int) {
        viewModelScope.launch {
            waterDataRepository.activeStartMinutes = value
            _activeStartMinutes.value = value
        }
    }

    override fun setActiveEndMinutes(value: Int) {
        viewModelScope.launch {
            waterDataRepository.activeEndMinutes = value
            _activeEndMinutes.value = value
        }
    }

    override fun setDisplayMode(value: DisplayMode) {
        viewModelScope.launch {
            waterDataRepository.displayMode = value
            _displayMode.value = value
        }
    }

    override fun setResetAtActiveStart(value: Boolean) {
        viewModelScope.launch {
            waterDataRepository.resetAtActiveStart = value
            _resetAtActiveStart.value = value
        }
    }

    override fun setSmartAdjust(value: Boolean) {
        viewModelScope.launch {
            waterDataRepository.smartAdjust = value
            _smartAdjust.value = value
        }
    }

    override fun setSnoozeMinutes(value: Int) {
        viewModelScope.launch {
            waterDataRepository.snoozeMinutes = value
            _snoozeMinutes.value = value
        }
    }

    override fun simulateReminder() {
        // Logic to trigger a notification will be added here
        Log.d("Debug", "Simulate Reminder clicked")
    }

    override fun clearTodaySchedule() {
        viewModelScope.launch {
            waterDataRepository.setDailySchedule(java.time.LocalDate.now(), com.kieronquinn.app.smartspacer.plugin.water.repositories.DailySchedule("", emptyList(), emptyList(), 0, 0))
            Log.d("Debug", "Cleared today's schedule")
        }
    }
}