package com.kieronquinn.app.smartspacer.plugin.qweather.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.qweather.complications.QWeatherComplication
import com.kieronquinn.app.smartspacer.plugin.qweather.providers.SettingsRepository
import com.kieronquinn.app.smartspacer.plugin.qweather.receivers.UpdateReceiver
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class SettingsViewModel : ViewModel() {
    abstract val state: StateFlow<State>
    abstract fun onApiKeyChanged(value: String)
    abstract fun onApiHostChanged(value: String)
    abstract fun onLocationNameChanged(value: String)
    abstract fun onIndicesChanged(value: Set<String>)
    abstract fun onUseEmojiChanged(value: Boolean)

    sealed class State {
        object Loading : State()
        data class Loaded(
            val apiKey: String,
            val apiHost: String,
            val locationName: String,
            val selectedIndices: Set<String>,
            val useEmoji: Boolean
        ) : State()
    }
}

class SettingsViewModelImpl(
    private val context: Context,
    private val settingsRepository: SettingsRepository
) : SettingsViewModel() {

    override val state = combine(
        settingsRepository.apiKey,
        settingsRepository.apiHost,
        settingsRepository.locationName,
        settingsRepository.selectedIndices,
        settingsRepository.useEmoji
    ) { apiKey, apiHost, locationName, selectedIndices, useEmoji ->
        State.Loaded(
            apiKey,
            apiHost,
            locationName,
            selectedIndices.split(",").filter { it.isNotEmpty() }.toSet(),
            useEmoji
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

    private suspend fun triggerUpdate() {
        withContext(Dispatchers.IO) {
            SmartspacerComplicationProvider.notifyChange(context, QWeatherComplication::class.java)
        }
        Log.d("QWeatherSettings", "Triggering UpdateReceiver...")
        val intent = Intent(context, UpdateReceiver::class.java).apply {
            putExtra(UpdateReceiver.EXTRA_SMARTSPACER_ID, "manual_update")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.sendBroadcast(intent)
    }
}
