package com.kieronquinn.app.smartspacer.plugin.qweather.providers

import android.content.Context
import androidx.core.content.edit
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.BaseSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.BaseSettingsRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

interface SettingsRepository : BaseSettingsRepository {
    val apiKey: Flow<String>
    val apiHost: Flow<String>
    val locationName: Flow<String>
    val selectedIndices: Flow<String>
    val cityLookupFailed: Flow<Boolean>
    var locationId: String?

    suspend fun setApiKey(value: String)
    suspend fun setApiHost(value: String)
    suspend fun setLocationName(value: String)
    suspend fun setSelectedIndices(value: String)
    suspend fun setCityLookupFailed(value: Boolean)
}

class SettingsRepositoryImpl(context: Context) : BaseSettingsRepositoryImpl(), SettingsRepository {
    companion object {
        private const val PREFERENCES_NAME = "qweather_prefs"
        private const val API_KEY_KEY = "api_key"
        private const val API_HOST_KEY = "api_host"
        private const val LOCATION_NAME_KEY = "location_name"
        private const val SELECTED_INDICES_KEY = "selected_indices"
        private const val CITY_LOOKUP_FAILED_KEY = "city_lookup_failed"
    }

    override val sharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private val _apiKey = MutableStateFlow(sharedPreferences.getString(API_KEY_KEY, "") ?: "")
    private val _apiHost = MutableStateFlow(sharedPreferences.getString(API_HOST_KEY, "") ?: "")
    private val _locationName = MutableStateFlow(sharedPreferences.getString(LOCATION_NAME_KEY, "") ?: "")
    private val _selectedIndices = MutableStateFlow(sharedPreferences.getString(SELECTED_INDICES_KEY, "1,2,3,5,9") ?: "1,2,3,5,9")
    private val _cityLookupFailed = MutableStateFlow(sharedPreferences.getBoolean(CITY_LOOKUP_FAILED_KEY, false))

    override val apiKey: Flow<String> = _apiKey.asStateFlow()
    override val apiHost: Flow<String> = _apiHost.asStateFlow()
    override val locationName: Flow<String> = _locationName.asStateFlow()
    override val selectedIndices: Flow<String> = _selectedIndices.asStateFlow()
    override val cityLookupFailed: Flow<Boolean> = _cityLookupFailed.asStateFlow()
    override var locationId: String? = null

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                API_KEY_KEY -> _apiKey.value = sharedPreferences.getString(API_KEY_KEY, "") ?: ""
                API_HOST_KEY -> _apiHost.value = sharedPreferences.getString(API_HOST_KEY, "") ?: ""
                LOCATION_NAME_KEY -> _locationName.value = sharedPreferences.getString(LOCATION_NAME_KEY, "") ?: ""
                SELECTED_INDICES_KEY -> _selectedIndices.value = sharedPreferences.getString(SELECTED_INDICES_KEY, "1,2,3,5,9") ?: "1,2,3,5,9"
                CITY_LOOKUP_FAILED_KEY -> _cityLookupFailed.value = sharedPreferences.getBoolean(CITY_LOOKUP_FAILED_KEY, false)
            }
        }
    }

    override suspend fun setApiKey(value: String) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putString(API_KEY_KEY, value).commit()
        _apiKey.value = value
    }

    override suspend fun setApiHost(value: String) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putString(API_HOST_KEY, value).commit()
        _apiHost.value = value
    }

    override suspend fun setLocationName(value: String) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().apply {
            putString(LOCATION_NAME_KEY, value)
            putBoolean(CITY_LOOKUP_FAILED_KEY, false)
            commit()
        }
        _locationName.value = value
        _cityLookupFailed.value = false
    }

    override suspend fun setSelectedIndices(value: String) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putString(SELECTED_INDICES_KEY, value).commit()
        _selectedIndices.value = value
    }

    override suspend fun setCityLookupFailed(value: Boolean) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putBoolean(CITY_LOOKUP_FAILED_KEY, value).commit()
        _cityLookupFailed.value = value
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
    this@getBlocking.first()
}
