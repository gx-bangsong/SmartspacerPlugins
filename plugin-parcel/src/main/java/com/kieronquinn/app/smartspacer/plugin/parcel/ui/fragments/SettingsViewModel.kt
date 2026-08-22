package com.kieronquinn.app.smartspacer.plugin.parcel.ui.fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.parcel.data.ParcelDao
import com.kieronquinn.app.smartspacer.plugin.parcel.data.RuleDao
import com.kieronquinn.app.smartspacer.plugin.parcel.notifications.ParcelLiveUpdatePublisher
import com.kieronquinn.app.smartspacer.plugin.parcel.repositories.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class SettingsViewModel : ViewModel() {
    abstract val state: StateFlow<State>
    abstract val cleanupDurationHours: StateFlow<Int>
    abstract val promotedLiveUpdates: StateFlow<Boolean>
    abstract fun onScanInboxClicked()
    abstract fun onImportRulesClicked()
    abstract fun setCleanupDurationHours(hours: Int)
    abstract fun setPromotedLiveUpdates(enabled: Boolean)

    sealed class State {
        object Loading : State()
        object Loaded : State()
    }
}

class SettingsViewModelImpl(
    private val parcelDao: ParcelDao,
    private val ruleDao: RuleDao,
    private val settingsRepository: SettingsRepository,
    private val liveUpdatePublisher: ParcelLiveUpdatePublisher
) : SettingsViewModel() {
    override val state = MutableStateFlow<State>(State.Loaded)

    private val _cleanupDurationHours = MutableStateFlow(24)
    override val cleanupDurationHours: StateFlow<Int> = _cleanupDurationHours

    private val _promotedLiveUpdates = MutableStateFlow(true)
    override val promotedLiveUpdates: StateFlow<Boolean> = _promotedLiveUpdates

    init {
        viewModelScope.launch {
            settingsRepository.cleanupDurationHours.collect {
                _cleanupDurationHours.value = it
            }
        }
        viewModelScope.launch {
            settingsRepository.promotedLiveUpdates.collect {
                _promotedLiveUpdates.value = it
            }
        }
    }

    override fun onScanInboxClicked() {
        // Handled in fragment for permissions
    }

    override fun onImportRulesClicked() {
        // Handled in fragment for picker
    }

    override fun setCleanupDurationHours(hours: Int) {
        viewModelScope.launch {
            settingsRepository.setCleanupDurationHours(hours)
        }
    }

    override fun setPromotedLiveUpdates(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setPromotedLiveUpdates(enabled)
            if (enabled) {
                liveUpdatePublisher.publishPending()
            }
        }
    }
}
