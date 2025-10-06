package com.kieronquinn.app.smartspacer.plugin.qweather.providers

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.BaseSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.BaseSettingsRepositoryImpl

interface SettingsRepository : BaseSettingsRepository {
    val apiKey: SmartspacerSetting<String>
    val locationId: SmartspacerSetting<String>
    val selectedIndices: SmartspacerSetting<String>
}

class SettingsRepositoryImpl(context: Context) : BaseSettingsRepositoryImpl(), SettingsRepository {
    companion object {
        private const val PREFERENCES_NAME = "qweather_prefs"
    }

    override val sharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override val apiKey = string("api_key", "")
    override val locationId = string("location_id", "")
    override val selectedIndices = string("selected_indices", "1,2,3,5,9") // Defaults

    override suspend fun getBackup(): Map<String, String> {
        return emptyMap() // Not implemented for this plugin
    }

    override suspend fun restoreBackup(settings: Map<String, String>) {
        // Not implemented for this plugin
    }
}