package com.kieronquinn.app.smartspacer.plugin.qweather.ui.screens.settings

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.qweather.complications.QWeatherComplication
import com.kieronquinn.app.smartspacer.plugin.qweather.providers.SettingsRepository
import com.kieronquinn.app.smartspacer.plugin.qweather.utils.AdvicePaging
import com.kieronquinn.app.smartspacer.plugin.qweather.work.QWeatherVisibilityWorker
import com.kieronquinn.app.smartspacer.plugin.qweather.work.QWeatherWorker
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class SettingsViewModel : ViewModel() {
    abstract val state: StateFlow<State>
    abstract fun onApiKeyChanged(value: String)
    abstract fun onApiHostChanged(value: String)
    abstract fun onLocationNameChanged(value: String)
    abstract fun onIndicesChanged(value: Set<String>)
    abstract fun onUseEmojiChanged(value: Boolean)
    abstract fun onPagingModeChanged(value: AdvicePaging)
    abstract fun onBedtimeEnabledChanged(value: Boolean)
    abstract fun onBedtimeMinutesChanged(value: Int)
    abstract fun onWakeEnabledChanged(value: Boolean)
    abstract fun onWakeMinutesChanged(value: Int)
    abstract fun onDisplayDurationChanged(value: Int)

    sealed class State {
        object Loading : State()
        data class Loaded(
            val apiKey: String,
            val apiHost: String,
            val locationName: String,
            val selectedIndices: Set<String>,
            val useEmoji: Boolean,
            val paging: AdvicePaging,
            val bedtimeEnabled: Boolean,
            val bedtimeMinutes: Int,
            val wakeEnabled: Boolean,
            val wakeMinutes: Int,
            val displayDurationMinutes: Int
        ) : State()
    }
}

class SettingsViewModelImpl(
    private val context: Context,
    private val settingsRepository: SettingsRepository
) : SettingsViewModel() {

    private val core = combine(
        settingsRepository.apiKey,
        settingsRepository.apiHost,
        settingsRepository.locationName,
        settingsRepository.selectedIndices,
        settingsRepository.useEmoji
    ) { apiKey, apiHost, locationName, selectedIndices, useEmoji ->
        Core(apiKey, apiHost, locationName, selectedIndices, useEmoji)
    }

    private val display = combine(
        settingsRepository.pagingMode,
        settingsRepository.bedtimeEnabled,
        settingsRepository.bedtimeMinutes,
        settingsRepository.wakeEnabled,
        settingsRepository.wakeMinutes
    ) { pagingMode, bedtimeEnabled, bedtimeMinutes, wakeEnabled, wakeMinutes ->
        Display(pagingMode, bedtimeEnabled, bedtimeMinutes, wakeEnabled, wakeMinutes)
    }

    override val state = combine(
        core,
        display,
        settingsRepository.displayDurationMinutes
    ) { coreState, displayState, duration ->
        State.Loaded(
            apiKey = coreState.apiKey,
            apiHost = coreState.apiHost,
            locationName = coreState.locationName,
            selectedIndices = coreState.selectedIndices.split(",").filter { it.isNotEmpty() }.toSet(),
            useEmoji = coreState.useEmoji,
            paging = AdvicePaging.fromPreference(displayState.pagingMode),
            bedtimeEnabled = displayState.bedtimeEnabled,
            bedtimeMinutes = displayState.bedtimeMinutes,
            wakeEnabled = displayState.wakeEnabled,
            wakeMinutes = displayState.wakeMinutes,
            displayDurationMinutes = duration
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), State.Loading)

    override fun onApiKeyChanged(value: String) {
        viewModelScope.launch {
            settingsRepository.setApiKey(value)
            triggerUpdate()
        }
    }

    override fun onApiHostChanged(value: String) {
        viewModelScope.launch {
            settingsRepository.setApiHost(value)
            triggerUpdate()
        }
    }

    override fun onLocationNameChanged(value: String) {
        viewModelScope.launch {
            settingsRepository.setLocationName(value)
            triggerUpdate()
        }
    }

    override fun onIndicesChanged(value: Set<String>) {
        viewModelScope.launch {
            settingsRepository.setSelectedIndices(value.joinToString(","))
            triggerUpdate()
        }
    }

    override fun onUseEmojiChanged(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setUseEmoji(value)
            triggerUpdate()
        }
    }

    override fun onPagingModeChanged(value: AdvicePaging) {
        viewModelScope.launch {
            settingsRepository.setPagingMode(value.prefValue)
            triggerUpdate()
        }
    }

    override fun onBedtimeEnabledChanged(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBedtimeEnabled(value)
            triggerVisibility()
        }
    }

    override fun onBedtimeMinutesChanged(value: Int) {
        viewModelScope.launch {
            settingsRepository.setBedtimeMinutes(value)
            triggerVisibility()
        }
    }

    override fun onWakeEnabledChanged(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setWakeEnabled(value)
            triggerVisibility()
        }
    }

    override fun onWakeMinutesChanged(value: Int) {
        viewModelScope.launch {
            settingsRepository.setWakeMinutes(value)
            triggerVisibility()
        }
    }

    override fun onDisplayDurationChanged(value: Int) {
        viewModelScope.launch {
            settingsRepository.setDisplayDurationMinutes(value)
            triggerVisibility()
        }
    }

    private suspend fun triggerVisibility() {
        withContext(Dispatchers.IO) {
            SmartspacerComplicationProvider.notifyChange(context, QWeatherComplication::class.java)
            QWeatherVisibilityWorker.scheduleNext(context, settingsRepository)
        }
    }

    private suspend fun triggerUpdate() {
        withContext(Dispatchers.IO) {
            SmartspacerComplicationProvider.notifyChange(context, QWeatherComplication::class.java)
        }
        Log.d("QWeatherSettings", "Triggering QWeatherWorker...")
        QWeatherWorker.enqueueImmediate(context)
        QWeatherWorker.enqueuePeriodic(context)
        QWeatherVisibilityWorker.scheduleNext(context, settingsRepository)
    }

    private data class Core(
        val apiKey: String,
        val apiHost: String,
        val locationName: String,
        val selectedIndices: String,
        val useEmoji: Boolean
    )

    private data class Display(
        val pagingMode: String,
        val bedtimeEnabled: Boolean,
        val bedtimeMinutes: Int,
        val wakeEnabled: Boolean,
        val wakeMinutes: Int
    )
}
