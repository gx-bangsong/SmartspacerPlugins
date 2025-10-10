package com.kieronquinn.app.smartspacer.plugin.qweather.ui.screens.settings

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.kieronquinn.app.smartspacer.plugin.qweather.R
import com.kieronquinn.app.smartspacer.plugin.qweather.complications.QWeatherComplication
import com.kieronquinn.app.smartspacer.plugin.qweather.providers.SettingsRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.koin.android.ext.android.inject

class SettingsFragment : PreferenceFragmentCompat() {

    private val settingsRepository by inject<SettingsRepository>()
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        setupApiKeyPreference()
        setupLocationIdPreference()
        setupIndicesPreference()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
    }

    private fun setupApiKeyPreference() {
        val apiKeyPreference = findPreference<EditTextPreference>("api_key") ?: return
        scope.launch {
            apiKeyPreference.text = settingsRepository.apiKey.first()
        }
        apiKeyPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            scope.launch {
                settingsRepository.setApiKey(newValue as String)
                triggerUpdate()
            }
            true
        }
    }

    private fun setupLocationIdPreference() {
        val locationIdPreference = findPreference<EditTextPreference>("location_id") ?: return
        scope.launch {
            locationIdPreference.text = settingsRepository.locationId.first()
        }
        locationIdPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            scope.launch {
                settingsRepository.setLocationId(newValue as String)
                triggerUpdate()
            }
            true
        }
    }

    private fun setupIndicesPreference() {
        val indicesPreference = findPreference<MultiSelectListPreference>("selected_indices") ?: return
        scope.launch {
            val currentValues = settingsRepository.selectedIndices.first()
            indicesPreference.values = currentValues.split(",").filter { it.isNotEmpty() }.toSet()
        }
        indicesPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            @Suppress("UNCHECKED_CAST")
            val selected = newValue as? Set<String> ?: return@OnPreferenceChangeListener false
            val commaSeparated = selected.joinToString(",")
            scope.launch {
                settingsRepository.setSelectedIndices(commaSeparated)
                triggerUpdate()
            }
            true
        }
    }

    private suspend fun triggerUpdate() {
        val context = context ?: return
        withContext(Dispatchers.IO) {
            SmartspacerComplicationProvider.notifyChange(context, QWeatherComplication::class.java)
        }
    }
}
