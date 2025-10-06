package com.kieronquinn.app.smartspacer.plugin.qweather.ui.screens.settings

import android.content.Intent
import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.kieronquinn.app.smartspacer.plugin.qweather.R
import com.kieronquinn.app.smartspacer.plugin.qweather.complications.QWeatherComplication
import com.kieronquinn.app.smartspacer.plugin.qweather.providers.SettingsRepository
import com.kieronquinn.app.smartspacer.plugin.qweather.receivers.UpdateReceiver
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class SettingsFragment : PreferenceFragmentCompat() {

    private val settingsRepository by inject<SettingsRepository>()
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        setupApiKeyPreference()
        setupLocationIdPreference()
        setupIndicesPreference()
    }

    private fun setupApiKeyPreference() {
        val apiKeyPreference = findPreference<EditTextPreference>("api_key") ?: return
        scope.launch {
            apiKeyPreference.text = settingsRepository.apiKey.get()
        }
        apiKeyPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            scope.launch {
                settingsRepository.apiKey.set(newValue as String)
                triggerUpdate()
            }
            true
        }
    }

    private fun setupLocationIdPreference() {
        val locationIdPreference = findPreference<EditTextPreference>("location_id") ?: return
        scope.launch {
            locationIdPreference.text = settingsRepository.locationId.get()
        }
        locationIdPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            scope.launch {
                settingsRepository.locationId.set(newValue as String)
                triggerUpdate()
            }
            true
        }
    }

    private fun setupIndicesPreference() {
        val indicesPreference = findPreference<MultiSelectListPreference>("selected_indices") ?: return
        scope.launch {
            val currentValues = settingsRepository.selectedIndices.get()
            indicesPreference.values = currentValues.split(",").filter { it.isNotEmpty() }.toSet()
        }
        indicesPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            @Suppress("UNCHECKED_CAST")
            val selected = newValue as? Set<String> ?: return@OnPreferenceChangeListener false
            val commaSeparated = selected.joinToString(",")
            scope.launch {
                settingsRepository.selectedIndices.set(commaSeparated)
                triggerUpdate()
            }
            true
        }
    }

    private suspend fun triggerUpdate() {
        val context = context ?: return
        withContext(Dispatchers.IO) {
            val componentName = QWeatherComplication().getComponentName(context)
            val activeComplications = SmartspacerComplicationProvider.getActiveComplications(context, componentName)
            activeComplications.forEach { smartspacerId ->
                val intent = Intent(context, UpdateReceiver::class.java).apply {
                    putExtra(UpdateReceiver.EXTRA_SMARTSPACER_ID, smartspacerId)
                }
                context.sendBroadcast(intent)
            }
        }
    }
}