package com.kieronquinn.app.smartspacer.plugin.travel.repositories

import android.content.Context
import androidx.core.content.edit
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.BaseSettingsRepositoryImpl
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kieronquinn.app.smartspacer.shared.smsparser.ParserRule
import java.util.regex.Pattern
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface TravelSettingsRepository {
    val isSmsParsingEnabled: Flow<Boolean>
    suspend fun setSmsParsingEnabled(enabled: Boolean)

    val isReadNotificationEnabled: Flow<Boolean>
    suspend fun setReadNotificationEnabled(enabled: Boolean)

    val jumpTarget: Flow<String>
    suspend fun setJumpTarget(target: String)

    val customRulesJson: Flow<String?>
    suspend fun setCustomRulesJson(json: String): Boolean
    suspend fun clearCustomRules()
}

class TravelSettingsRepositoryImpl(context: Context) : BaseSettingsRepositoryImpl(), TravelSettingsRepository {
    companion object {
        private const val PREFERENCES_NAME = "travel_prefs"
        private const val KEY_SMS_PARSING = "sms_parsing_enabled"
        private const val KEY_READ_NOTIFICATION = "read_notification_enabled"
        private const val KEY_JUMP_TARGET = "jump_target"
        private const val KEY_CUSTOM_RULES = "custom_rules_json"
    }

    override val sharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private val _isSmsParsingEnabled = MutableStateFlow(sharedPreferences.getBoolean(KEY_SMS_PARSING, true))
    override val isSmsParsingEnabled = _isSmsParsingEnabled.asStateFlow()

    private val _isReadNotificationEnabled = MutableStateFlow(sharedPreferences.getBoolean(KEY_READ_NOTIFICATION, false))
    override val isReadNotificationEnabled = _isReadNotificationEnabled.asStateFlow()

    private val _jumpTarget = MutableStateFlow(sharedPreferences.getString(KEY_JUMP_TARGET, "auto") ?: "auto")
    override val jumpTarget = _jumpTarget.asStateFlow()

    private val _customRulesJson = MutableStateFlow(sharedPreferences.getString(KEY_CUSTOM_RULES, null))
    override val customRulesJson = _customRulesJson.asStateFlow()

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener { prefs, key ->
            when (key) {
                KEY_SMS_PARSING -> _isSmsParsingEnabled.value = prefs.getBoolean(KEY_SMS_PARSING, true)
                KEY_READ_NOTIFICATION -> _isReadNotificationEnabled.value = prefs.getBoolean(KEY_READ_NOTIFICATION, false)
                KEY_JUMP_TARGET -> _jumpTarget.value = prefs.getString(KEY_JUMP_TARGET, "auto") ?: "auto"
                KEY_CUSTOM_RULES -> _customRulesJson.value = prefs.getString(KEY_CUSTOM_RULES, null)
            }
        }
    }

    override suspend fun setSmsParsingEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        sharedPreferences.edit { putBoolean(KEY_SMS_PARSING, enabled) }
    }

    override suspend fun setReadNotificationEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        sharedPreferences.edit { putBoolean(KEY_READ_NOTIFICATION, enabled) }
    }

    override suspend fun setJumpTarget(target: String) = withContext(Dispatchers.IO) {
        sharedPreferences.edit { putString(KEY_JUMP_TARGET, target) }
    }

    override suspend fun setCustomRulesJson(json: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val type = object : TypeToken<List<ParserRule>>() {}.type
            val rules: List<ParserRule> = Gson().fromJson(json, type)
            require(rules.isNotEmpty())
            rules.forEach { Pattern.compile(it.pattern) }
            sharedPreferences.edit { putString(KEY_CUSTOM_RULES, json) }
            true
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun clearCustomRules() = withContext(Dispatchers.IO) {
        sharedPreferences.edit { remove(KEY_CUSTOM_RULES) }
    }

    override suspend fun getBackup(): Map<String, String> = emptyMap()
    override suspend fun restoreBackup(settings: Map<String, String>) {}
}
