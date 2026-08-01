package com.kieronquinn.app.smartspacer.plugin.checkin.ui.fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.checkin.data.CheckInDao
import com.kieronquinn.app.smartspacer.plugin.checkin.data.CheckInItem
import com.kieronquinn.app.smartspacer.plugin.checkin.repositories.CheckInSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.checkin.repositories.CheckInScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CheckInSettingsViewModel(
    private val settingsRepository: CheckInSettingsRepository,
    private val checkInDao: CheckInDao,
    private val scheduler: CheckInScheduler
) : ViewModel() {

    val isReminderEnabled = settingsRepository.isReminderEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

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

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setReminderEnabled(enabled)
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
