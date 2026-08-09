package com.kieronquinn.app.smartspacer.plugin.travel.ui.fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.travel.data.TravelInfoDao
import com.kieronquinn.app.smartspacer.plugin.travel.data.TravelInfoItem
import com.kieronquinn.app.smartspacer.plugin.travel.repositories.TravelSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.travel.repositories.TravelScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TravelSettingsViewModel(
    private val settingsRepository: TravelSettingsRepository,
    private val travelInfoDao: TravelInfoDao,
    private val travelScheduler: TravelScheduler
) : ViewModel() {

    val isSmsParsingEnabled = settingsRepository.isSmsParsingEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val isReadNotificationEnabled = settingsRepository.isReadNotificationEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val jumpTarget = settingsRepository.jumpTarget
        .stateIn(viewModelScope, SharingStarted.Eagerly, "none")

    val allTrips = travelInfoDao.getAllFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun setSmsParsingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSmsParsingEnabled(enabled)
        }
    }

    fun setReadNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setReadNotificationEnabled(enabled)
        }
    }

    fun setJumpTarget(target: String) {
        viewModelScope.launch {
            settingsRepository.setJumpTarget(target)
        }
    }

    fun deleteTrip(item: TravelInfoItem) {
        viewModelScope.launch {
            travelInfoDao.delete(item)
            travelScheduler.cancelReminder(item.id)
        }
    }

    fun addTrip(item: TravelInfoItem) {
        viewModelScope.launch {
            travelInfoDao.insert(item)
            travelScheduler.scheduleReminder(item)
        }
    }
}
