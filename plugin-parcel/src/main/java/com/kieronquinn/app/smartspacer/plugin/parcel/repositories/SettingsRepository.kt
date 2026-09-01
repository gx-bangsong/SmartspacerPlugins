package com.kieronquinn.app.smartspacer.plugin.parcel.repositories

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
    val cleanupDurationHours: Flow<Int>
    suspend fun setCleanupDurationHours(value: Int)

    /**
     * EXPERIMENTAL, default ON: renders pickup-code notifications as promoted Live Updates.
     * Officially, ordinary package tracking is not recommended for promoted notifications; this
     * toggle lets power users opt in while keeping the default behaviour compliant.
     */
    val promotedLiveUpdates: Flow<Boolean>
    suspend fun setPromotedLiveUpdates(enabled: Boolean)
}

class SettingsRepositoryImpl(context: Context) : BaseSettingsRepositoryImpl(), SettingsRepository {
    companion object {
        private const val PREFERENCES_NAME = "parcel_prefs"
        private const val CLEANUP_DURATION_KEY = "cleanup_duration_hours"
        private const val DEFAULT_CLEANUP_DURATION = 24
        private const val PROMOTED_LIVE_UPDATES_KEY = "promoted_live_updates"
        private const val DEFAULT_PROMOTED_LIVE_UPDATES = true
    }

    override val sharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private val _cleanupDurationHours = MutableStateFlow(sharedPreferences.getInt(CLEANUP_DURATION_KEY, DEFAULT_CLEANUP_DURATION))
    override val cleanupDurationHours: Flow<Int> = _cleanupDurationHours.asStateFlow()

    private val _promotedLiveUpdates = MutableStateFlow(sharedPreferences.getBoolean(PROMOTED_LIVE_UPDATES_KEY, DEFAULT_PROMOTED_LIVE_UPDATES))
    override val promotedLiveUpdates: Flow<Boolean> = _promotedLiveUpdates.asStateFlow()

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                CLEANUP_DURATION_KEY -> _cleanupDurationHours.value = sharedPreferences.getInt(CLEANUP_DURATION_KEY, DEFAULT_CLEANUP_DURATION)
                PROMOTED_LIVE_UPDATES_KEY -> _promotedLiveUpdates.value = sharedPreferences.getBoolean(PROMOTED_LIVE_UPDATES_KEY, DEFAULT_PROMOTED_LIVE_UPDATES)
            }
        }
    }

    override suspend fun setCleanupDurationHours(value: Int) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putInt(CLEANUP_DURATION_KEY, value).commit()
        _cleanupDurationHours.value = value
    }

    override suspend fun setPromotedLiveUpdates(enabled: Boolean) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putBoolean(PROMOTED_LIVE_UPDATES_KEY, enabled).commit()
        _promotedLiveUpdates.value = enabled
    }

    override suspend fun getBackup(): Map<String, String> = emptyMap()
    override suspend fun restoreBackup(settings: Map<String, String>) {}
}

fun <T> Flow<T>.getBlocking(): T = runBlocking {
    this@getBlocking.first()
}
