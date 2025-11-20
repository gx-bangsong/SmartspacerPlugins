package com.kieronquinn.app.smartspacer.plugin.qweather.ui.screens.settings

import android.content.Intent // 新增导入
import android.os.Bundle
import android.util.Log // 新增导入
import androidx.preference.EditTextPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.kieronquinn.app.smartspacer.plugin.qweather.R
import com.kieronquinn.app.smartspacer.plugin.qweather.complications.QWeatherComplication
import com.kieronquinn.app.smartspacer.plugin.qweather.providers.SettingsRepository
import com.kieronquinn.app.smartspacer.plugin.qweather.receivers.UpdateReceiver // 确保导入你的 Receiver
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
        setupApiHostPreference()
        setupLocationNamePreference()
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

    private fun setupApiHostPreference() {
        val apiHostPreference = findPreference<EditTextPreference>("api_host") ?: return
        scope.launch {
            apiHostPreference.text = settingsRepository.apiHost.first()
        }
        apiHostPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            scope.launch {
                settingsRepository.setApiHost(newValue as String)
                triggerUpdate()
            }
            true
        }
    }

    private fun setupLocationNamePreference() {
        val locationNamePreference = findPreference<EditTextPreference>("location_name") ?: return
        scope.launch {
            locationNamePreference.text = settingsRepository.locationName.first()
        }
        locationNamePreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            scope.launch {
                settingsRepository.setLocationName(newValue as String)
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

    // 【关键修改】这里改动了
    private suspend fun triggerUpdate() {
        val context = context ?: return
        
        // 1. 通知 Smartspacer 宿主刷新 UI (保持原样)
        withContext(Dispatchers.IO) {
            SmartspacerComplicationProvider.notifyChange(context, QWeatherComplication::class.java)
        }

        // 2. 【新增】手动发送广播，强制唤醒 UpdateReceiver 进行网络请求
        Log.d("QWeatherSettings", "正在手动触发 UpdateReceiver...")
        val intent = Intent(context, UpdateReceiver::class.java)
        // 传入一个标记，告诉 Receiver 这是手动更新（可选）
        intent.putExtra(UpdateReceiver.EXTRA_SMARTSPACER_ID, "manual_update")
        context.sendBroadcast(intent)
    }
}
