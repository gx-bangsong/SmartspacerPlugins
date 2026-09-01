package com.kieronquinn.app.smartspacer.plugin.qweather.providers

import android.content.Context
import androidx.core.content.edit
import com.kieronquinn.app.smartspacer.plugin.qweather.utils.AdvicePaging
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
    val useEmoji: Flow<Boolean>
    val pagingMode: Flow<String>
    val pageLimit: Flow<Int>
    val bedtimeEnabled: Flow<Boolean>
    val bedtimeMinutes: Flow<Int>
    val wakeEnabled: Flow<Boolean>
    val wakeMinutes: Flow<Int>
    val displayDurationMinutes: Flow<Int>
    val cityLookupFailed: Flow<Boolean>
    var locationId: String?

    suspend fun setApiKey(value: String)
    suspend fun setApiHost(value: String)
    suspend fun setLocationName(value: String)
    suspend fun setSelectedIndices(value: String)
    suspend fun setUseEmoji(value: Boolean)
    suspend fun setPagingMode(value: String)
    suspend fun setPageLimit(value: Int)
    suspend fun setBedtimeEnabled(value: Boolean)
    suspend fun setBedtimeMinutes(value: Int)
    suspend fun setWakeEnabled(value: Boolean)
    suspend fun setWakeMinutes(value: Int)
    suspend fun setDisplayDurationMinutes(value: Int)
    suspend fun setCityLookupFailed(value: Boolean)
}

class SettingsRepositoryImpl(context: Context) : BaseSettingsRepositoryImpl(), SettingsRepository {
    companion object {
        private const val PREFERENCES_NAME = "qweather_prefs"
        private const val API_KEY_KEY = "api_key"
        private const val API_HOST_KEY = "api_host"
        private const val LOCATION_NAME_KEY = "location_name"
        private const val SELECTED_INDICES_KEY = "selected_indices"
        private const val USE_EMOJI_KEY = "use_emoji"
        private const val PAGING_MODE_KEY = "paging_mode"
        private const val PAGE_LIMIT_KEY = "page_limit"
        private const val BEDTIME_ENABLED_KEY = "bedtime_enabled"
        private const val BEDTIME_MINUTES_KEY = "bedtime_minutes"
        private const val WAKE_ENABLED_KEY = "wake_enabled"
        private const val WAKE_MINUTES_KEY = "wake_minutes"
        private const val DISPLAY_DURATION_KEY = "display_duration_minutes"
        private const val CITY_LOOKUP_FAILED_KEY = "city_lookup_failed"
        private const val DEFAULT_BEDTIME_MINUTES = 22 * 60
        private const val DEFAULT_WAKE_MINUTES = 7 * 60
        private const val DEFAULT_DISPLAY_DURATION = 60
    }

    override val sharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private val _apiKey = MutableStateFlow(sharedPreferences.getString(API_KEY_KEY, "") ?: "")
    private val _apiHost = MutableStateFlow(sharedPreferences.getString(API_HOST_KEY, "") ?: "")
    private val _locationName = MutableStateFlow(sharedPreferences.getString(LOCATION_NAME_KEY, "") ?: "")
    private val _selectedIndices = MutableStateFlow(sharedPreferences.getString(SELECTED_INDICES_KEY, "1,2,3,5,9") ?: "1,2,3,5,9")
    private val _useEmoji = MutableStateFlow(sharedPreferences.getBoolean(USE_EMOJI_KEY, false))
    private val _pagingMode = MutableStateFlow(
        sharedPreferences.getString(PAGING_MODE_KEY, AdvicePaging.DEFAULT_PREF) ?: AdvicePaging.DEFAULT_PREF
    )
    private val _pageLimit = MutableStateFlow(
        AdvicePaging.sanitizeLimit(
            sharedPreferences.getInt(PAGE_LIMIT_KEY, AdvicePaging.DEFAULT_LIMIT)
        )
    )
    private val _bedtimeEnabled = MutableStateFlow(sharedPreferences.getBoolean(BEDTIME_ENABLED_KEY, false))
    private val _bedtimeMinutes = MutableStateFlow(
        sharedPreferences.getInt(BEDTIME_MINUTES_KEY, DEFAULT_BEDTIME_MINUTES)
    )
    private val _wakeEnabled = MutableStateFlow(sharedPreferences.getBoolean(WAKE_ENABLED_KEY, false))
    private val _wakeMinutes = MutableStateFlow(
        sharedPreferences.getInt(WAKE_MINUTES_KEY, DEFAULT_WAKE_MINUTES)
    )
    private val _displayDurationMinutes = MutableStateFlow(
        sharedPreferences.getInt(DISPLAY_DURATION_KEY, DEFAULT_DISPLAY_DURATION)
    )
    private val _cityLookupFailed = MutableStateFlow(sharedPreferences.getBoolean(CITY_LOOKUP_FAILED_KEY, false))

    override val apiKey: Flow<String> = _apiKey.asStateFlow()
    override val apiHost: Flow<String> = _apiHost.asStateFlow()
    override val locationName: Flow<String> = _locationName.asStateFlow()
    override val selectedIndices: Flow<String> = _selectedIndices.asStateFlow()
    override val useEmoji: Flow<Boolean> = _useEmoji.asStateFlow()
    override val pagingMode: Flow<String> = _pagingMode.asStateFlow()
    override val pageLimit: Flow<Int> = _pageLimit.asStateFlow()
    override val bedtimeEnabled: Flow<Boolean> = _bedtimeEnabled.asStateFlow()
    override val bedtimeMinutes: Flow<Int> = _bedtimeMinutes.asStateFlow()
    override val wakeEnabled: Flow<Boolean> = _wakeEnabled.asStateFlow()
    override val wakeMinutes: Flow<Int> = _wakeMinutes.asStateFlow()
    override val displayDurationMinutes: Flow<Int> = _displayDurationMinutes.asStateFlow()
    override val cityLookupFailed: Flow<Boolean> = _cityLookupFailed.asStateFlow()
    override var locationId: String? = null

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                API_KEY_KEY -> _apiKey.value = sharedPreferences.getString(API_KEY_KEY, "") ?: ""
                API_HOST_KEY -> _apiHost.value = sharedPreferences.getString(API_HOST_KEY, "") ?: ""
                LOCATION_NAME_KEY -> _locationName.value = sharedPreferences.getString(LOCATION_NAME_KEY, "") ?: ""
                SELECTED_INDICES_KEY -> _selectedIndices.value = sharedPreferences.getString(SELECTED_INDICES_KEY, "1,2,3,5,9") ?: "1,2,3,5,9"
                USE_EMOJI_KEY -> _useEmoji.value = sharedPreferences.getBoolean(USE_EMOJI_KEY, false)
                PAGING_MODE_KEY -> _pagingMode.value =
                    sharedPreferences.getString(PAGING_MODE_KEY, AdvicePaging.DEFAULT_PREF)
                        ?: AdvicePaging.DEFAULT_PREF
                PAGE_LIMIT_KEY -> _pageLimit.value = AdvicePaging.sanitizeLimit(
                    sharedPreferences.getInt(PAGE_LIMIT_KEY, AdvicePaging.DEFAULT_LIMIT)
                )
                BEDTIME_ENABLED_KEY -> _bedtimeEnabled.value =
                    sharedPreferences.getBoolean(BEDTIME_ENABLED_KEY, false)
                BEDTIME_MINUTES_KEY -> _bedtimeMinutes.value =
                    sharedPreferences.getInt(BEDTIME_MINUTES_KEY, DEFAULT_BEDTIME_MINUTES)
                WAKE_ENABLED_KEY -> _wakeEnabled.value =
                    sharedPreferences.getBoolean(WAKE_ENABLED_KEY, false)
                WAKE_MINUTES_KEY -> _wakeMinutes.value =
                    sharedPreferences.getInt(WAKE_MINUTES_KEY, DEFAULT_WAKE_MINUTES)
                DISPLAY_DURATION_KEY -> _displayDurationMinutes.value =
                    sharedPreferences.getInt(DISPLAY_DURATION_KEY, DEFAULT_DISPLAY_DURATION)
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

    override suspend fun setUseEmoji(value: Boolean) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putBoolean(USE_EMOJI_KEY, value).commit()
        _useEmoji.value = value
    }

    override suspend fun setPagingMode(value: String) = withContext(Dispatchers.IO) {
        val preset = AdvicePaging.fromPreference(value)
        sharedPreferences.edit()
            .putString(PAGING_MODE_KEY, preset.prefValue)
            .putInt(PAGE_LIMIT_KEY, preset.maxChars)
            .commit()
        _pagingMode.value = preset.prefValue
        _pageLimit.value = preset.maxChars
    }

    override suspend fun setPageLimit(value: Int) = withContext(Dispatchers.IO) {
        val limit = AdvicePaging.sanitizeLimit(value)
        sharedPreferences.edit().putInt(PAGE_LIMIT_KEY, limit).commit()
        _pageLimit.value = limit
    }

    override suspend fun setBedtimeEnabled(value: Boolean) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putBoolean(BEDTIME_ENABLED_KEY, value).commit()
        _bedtimeEnabled.value = value
    }

    override suspend fun setBedtimeMinutes(value: Int) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putInt(BEDTIME_MINUTES_KEY, value).commit()
        _bedtimeMinutes.value = value
    }

    override suspend fun setWakeEnabled(value: Boolean) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putBoolean(WAKE_ENABLED_KEY, value).commit()
        _wakeEnabled.value = value
    }

    override suspend fun setWakeMinutes(value: Int) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putInt(WAKE_MINUTES_KEY, value).commit()
        _wakeMinutes.value = value
    }

    override suspend fun setDisplayDurationMinutes(value: Int) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putInt(DISPLAY_DURATION_KEY, value).commit()
        _displayDurationMinutes.value = value
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
