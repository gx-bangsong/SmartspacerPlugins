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
        scope.cancel() // 取消协程，防止内存泄漏
    }

    private fun setupApiKeyPreference() {
        val apiKeyPreference = findPreference<EditTextPreference>("api_key") ?: return
        scope.launch {
            // 使用 Flow.first() 获取初始值
            apiKeyPreference.text = settingsRepository.apiKey.first()
        }
        apiKeyPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            scope.launch {
                // 使用 SettingsRepository 中的 suspend 方法
                settingsRepository.setApiKey(newValue as String)
                triggerUpdate()
            }
            true
        }
    }

    private fun setupLocationIdPreference() {
        val locationIdPreference = findPreference<EditTextPreference>("location_id") ?: return
        scope.launch {
            // 使用 Flow.first() 获取初始值
            locationIdPreference.text = settingsRepository.locationId.first()
        }
        locationIdPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            scope.launch {
                // 使用 SettingsRepository 中的 suspend 方法
                settingsRepository.setLocationId(newValue as String)
                triggerUpdate()
            }
            true
        }
    }

    private fun setupIndicesPreference() {
        val indicesPreference = findPreference<MultiSelectListPreference>("selected_indices") ?: return
        scope.launch {
            // 使用 Flow.first() 获取初始值
            val currentValues = settingsRepository.selectedIndices.first()
            indicesPreference.values = currentValues.split(",").filter { it.isNotEmpty() }.toSet()
        }
        indicesPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            @Suppress("UNCHECKED_CAST")
            val selected = newValue as? Set<String> ?: return@OnPreferenceChangeListener false
            val commaSeparated = selected.joinToString(",")
            scope.launch {
                // 使用 SettingsRepository 中的 suspend 方法
                settingsRepository.setSelectedIndices(commaSeparated)
                triggerUpdate()
            }
            true
        }
    }

    private suspend fun triggerUpdate() {
        val context = context ?: return
        withContext(Dispatchers.IO) {
            // 调用 notifyChange 来通知 Smartspacer 更新
            SmartspacerComplicationProvider.notifyChange(context, QWeatherComplication::class.java)
        }
    }
}
