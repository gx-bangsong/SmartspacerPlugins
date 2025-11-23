package com.kieronquinn.app.smartspacer.plugin.water.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.water.repositories.DisplayMode
import com.kieronquinn.app.smartspacer.plugin.water.repositories.WaterDataRepository
import com.kieronquinn.app.smartspacer.plugin.water.scheduling.WaterScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class WaterSettingsViewModel : ViewModel() {
    abstract val uiState: Flow<UiState>

    val initialState: UiState
        get() = UiState(0, 0, 0, 0, 0, 0, DisplayMode.DYNAMIC, false, false, 0)

    abstract fun onDailyGoalChanged(newValue: Float)
    abstract fun onCupSizeChanged(newValue: Float)
    abstract fun onStartTimeChanged(hour: Int, minute: Int)
    abstract fun onEndTimeChanged(hour: Int, minute: Int)
    abstract fun onDisplayModeChanged(displayMode: DisplayMode)
    abstract fun onResetAtActiveStartChanged(checked: Boolean)
    abstract fun onSmartAdjustChanged(checked: Boolean)
    abstract fun onSnoozeMinutesChanged(newValue: Float)
    abstract fun saveChanges(context: Context)

    data class UiState(
        val dailyGoalMl: Int,
        val cupSizeMl: Int,
        val startTimeHour: Int,
        val startTimeMinute: Int,
        val endTimeHour: Int,
        val endTimeMinute: Int,
        val displayMode: DisplayMode,
        val resetAtActiveStart: Boolean,
        val smartAdjust: Boolean,
        val snoozeMinutes: Int
    )
}

class WaterSettingsViewModelImpl(
    private val waterDataRepository: WaterDataRepository
) : WaterSettingsViewModel(), KoinComponent {

    private val waterScheduler by inject<WaterScheduler>()

    private val _uiState = MutableStateFlow(
        UiState(
            dailyGoalMl = waterDataRepository.dailyGoalMl,
            cupSizeMl = waterDataRepository.cupMl,
            startTimeHour = waterDataRepository.activeStartMinutes / 60,
            startTimeMinute = waterDataRepository.activeStartMinutes % 60,
            endTimeHour = waterDataRepository.activeEndMinutes / 60,
            endTimeMinute = waterDataRepository.activeEndMinutes % 60,
            displayMode = waterDataRepository.displayMode,
            resetAtActiveStart = waterDataRepository.resetAtActiveStart,
            smartAdjust = waterDataRepository.smartAdjust,
            snoozeMinutes = waterDataRepository.snoozeMinutes
        )
    )
    override val uiState = _uiState.asStateFlow()

    override fun onDailyGoalChanged(newValue: Float) {
        viewModelScope.launch {
            _uiState.emit(uiState.value.copy(dailyGoalMl = newValue.toInt()))
        }
    }

    override fun onCupSizeChanged(newValue: Float) {
        viewModelScope.launch {
            _uiState.emit(uiState.value.copy(cupSizeMl = newValue.toInt()))
        }
    }

    override fun onStartTimeChanged(hour: Int, minute: Int) {
        viewModelScope.launch {
            _uiState.emit(uiState.value.copy(startTimeHour = hour, startTimeMinute = minute))
        }
    }

    override fun onEndTimeChanged(hour: Int, minute: Int) {
        viewModelScope.launch {
            _uiState.emit(uiState.value.copy(endTimeHour = hour, endTimeMinute = minute))
        }
    }

    override fun onDisplayModeChanged(displayMode: DisplayMode) {
        viewModelScope.launch {
            _uiState.emit(uiState.value.copy(displayMode = displayMode))
        }
    }

    override fun onResetAtActiveStartChanged(checked: Boolean) {
        viewModelScope.launch {
            _uiState.emit(uiState.value.copy(resetAtActiveStart = checked))
        }
    }

    override fun onSmartAdjustChanged(checked: Boolean) {
        viewModelScope.launch {
            _uiState.emit(uiState.value.copy(smartAdjust = checked))
        }
    }

    override fun onSnoozeMinutesChanged(newValue: Float) {
        viewModelScope.launch {
            _uiState.emit(uiState.value.copy(snoozeMinutes = newValue.toInt()))
        }
    }

    override fun saveChanges(context: Context) {
        waterDataRepository.dailyGoalMl = uiState.value.dailyGoalMl
        waterDataRepository.cupMl = uiState.value.cupSizeMl
        waterDataRepository.activeStartMinutes = uiState.value.startTimeHour * 60 + uiState.value.startTimeMinute
        waterDataRepository.activeEndMinutes = uiState.value.endTimeHour * 60 + uiState.value.endTimeMinute
        waterDataRepository.displayMode = uiState.value.displayMode
        waterDataRepository.resetAtActiveStart = uiState.value.resetAtActiveStart
        waterDataRepository.smartAdjust = uiState.value.smartAdjust
        waterDataRepository.snoozeMinutes = uiState.value.snoozeMinutes

        val today = java.time.LocalDate.now()
        val totalCups = kotlin.math.ceil(waterDataRepository.dailyGoalMl.toDouble() / waterDataRepository.cupMl).toInt()
        val schedule = waterScheduler.computeDailySchedule(
            today,
            waterDataRepository.activeStartMinutes,
            waterDataRepository.activeEndMinutes,
            totalCups
        )
        waterScheduler.scheduleAlarmsForDate(context, schedule)
    }
}
