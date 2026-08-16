package com.kieronquinn.app.smartspacer.plugin.checkin.ui.fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.checkin.data.CheckInDao
import com.kieronquinn.app.smartspacer.plugin.checkin.data.CheckInItem
import com.kieronquinn.app.smartspacer.plugin.checkin.repositories.CheckInSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.checkin.repositories.CheckInScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CheckInUiState(
    val records: List<CheckInItem>,
    val reminderEnabled: Boolean,
    val checkInOnly: Boolean,
    val workStartTime: String,
    val workEndTime: String,
    val customReminderText: String,
    val linkApp: String
)

class CheckInSettingsViewModel(
    private val settingsRepository: CheckInSettingsRepository,
    private val checkInDao: CheckInDao,
    private val scheduler: CheckInScheduler
) : ViewModel() {

    val isReminderEnabled = settingsRepository.isReminderEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val checkInOnly = settingsRepository.checkInOnly
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val workStartTime = settingsRepository.workStartTime
        .stateIn(viewModelScope, SharingStarted.Eagerly, "08:30")

    val workEndTime = settingsRepository.workEndTime
        .stateIn(viewModelScope, SharingStarted.Eagerly, "17:30")

    val customReminderText = settingsRepository.customReminderText
        .stateIn(viewModelScope, SharingStarted.Eagerly, "上班时间请记得打卡")

    val linkApp = settingsRepository.linkApp
        .stateIn(viewModelScope, SharingStarted.Eagerly, "none")

    val historyList = checkInDao.getAllFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * Combined state of every setting plus the history list. The settings
     * screen rebuilds whenever any of these emit, so selecting a new time
     * immediately updates the displayed value instead of waiting for the
     * history list to change.
     */
    val uiState: StateFlow<CheckInUiState> = combine(
        combine(historyList, isReminderEnabled) { records, enabled -> records to enabled },
        combine(checkInOnly, workStartTime, workEndTime) { only, start, end -> Triple(only, start, end) },
        combine(customReminderText, linkApp) { text, app -> text to app }
    ) { (records, enabled), (only, start, end), (text, app) ->
        CheckInUiState(
            records = records,
            reminderEnabled = enabled,
            checkInOnly = only,
            workStartTime = start,
            workEndTime = end,
            customReminderText = text,
            linkApp = app
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        CheckInUiState(emptyList(), true, false, "08:30", "17:30", "上班时间请记得打卡", "none")
    )

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setReminderEnabled(enabled)
            scheduler.scheduleDailyAlarms()
        }
    }

    fun setCheckInOnly(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setCheckInOnly(enabled)
            scheduler.scheduleDailyAlarms()
        }
    }

    fun setWorkStartTime(time: String) {
        viewModelScope.launch {
            settingsRepository.setWorkStartTime(time)
            scheduler.scheduleDailyAlarms()
        }
    }

    fun setWorkEndTime(time: String) {
        viewModelScope.launch {
            settingsRepository.setWorkEndTime(time)
            scheduler.scheduleDailyAlarms()
        }
    }

    fun setCustomReminderText(text: String) {
        viewModelScope.launch {
            settingsRepository.setCustomReminderText(text)
        }
    }

    fun setLinkApp(app: String) {
        viewModelScope.launch {
            settingsRepository.setLinkApp(app)
        }
    }

    fun deleteRecord(item: CheckInItem) {
        viewModelScope.launch {
            checkInDao.delete(item)
        }
    }

    fun addRecord(item: CheckInItem) {
        viewModelScope.launch {
            checkInDao.insert(item)
        }
    }
}
