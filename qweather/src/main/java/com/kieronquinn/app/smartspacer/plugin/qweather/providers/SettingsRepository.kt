package com.kieronquinn.app.smartspacer.plugin.qweather.providers

import android.content.Context
import androidx.core.content.edit
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.BaseSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.BaseSettingsRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

interface SettingsRepository : BaseSettingsRepository {
    val apiKey: Flow<String>
    val locationId: Flow<String>
    val selectedIndices: Flow<String>

    suspend fun setApiKey(value: String)
    suspend fun setLocationId(value: String)
    suspend fun setSelectedIndices(value: String)
}

class SettingsRepositoryImpl(context: Context) : BaseSettingsRepositoryImpl(), SettingsRepository {
    companion object {
        private const val PREFERENCES_NAME = "qweather_prefs"
        private const val API_KEY_KEY = "api_key"
        private const val LOCATION_ID_KEY = "location_id"
        private const val SELECTED_INDICES_KEY = "selected_indices"
    }

    override val sharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private val _apiKey = MutableStateFlow(sharedPreferences.getString(API_KEY_KEY, "") ?: "")
    private val _locationId = MutableStateFlow(sharedPreferences.getString(LOCATION_ID_KEY, "") ?: "")
    private val _selectedIndices = MutableStateFlow(sharedPreferences.getString(SELECTED_INDICES_KEY, "1,2,3,5,9") ?: "1,2,3,5,9")

    override val apiKey: Flow<String> = _apiKey.asStateFlow()
    override val locationId: Flow<String> = _locationId.asStateFlow()
    override val selectedIndices: Flow<String> = _selectedIndices.asStateFlow()

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                API_KEY_KEY -> _apiKey.value = sharedPreferences.getString(API_KEY_KEY, "") ?: ""
                LOCATION_ID_KEY -> _locationId.value = sharedPreferences.getString(LOCATION_ID_KEY, "") ?: ""
                SELECTED_INDICES_KEY -> _selectedIndices.value = sharedPreferences.getString(SELECTED_INDICES_KEY, "1,2,3,5,9") ?: "1,2,3,5,9"
            }
        }
    }

    override suspend fun setApiKey(value: String) {
        sharedPreferences.edit { putString(API_KEY_KEY, value) }
    }

    override suspend fun setLocationId(value: String) {
        sharedPreferences.edit { putString(LOCATION_ID_KEY, value) }
    }

    override suspend fun setSelectedIndices(value: String) {
        sharedPreferences.edit { putString(SELECTED_INDICES_KEY, value) }
    }

    override suspend fun getBackup(): Map<String, String> {
        return emptyMap()
    }

    override suspend fun restoreBackup(settings: Map<String, String>) {
        // Not implemented for this plugin
    }
}

// 修正 getBlocking 扩展函数，确保它正确返回 Flow.first() 的结果
fun <T> Flow<T>.getBlocking(): T = runBlocking {
    this.first()
}
