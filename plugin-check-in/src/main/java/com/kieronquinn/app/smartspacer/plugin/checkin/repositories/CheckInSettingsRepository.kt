package com.kieronquinn.app.smartspacer.plugin.checkin.repositories

import android.content.Context
import androidx.core.content.edit
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.BaseSettingsRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface CheckInSettingsRepository {
    val isReminderEnabled: Flow<Boolean>
    suspend fun setReminderEnabled(enabled: Boolean)

    val checkInOnly: Flow<Boolean>
    suspend fun setCheckInOnly(enabled: Boolean)

    val workStartTime: Flow<String>
    suspend fun setWorkStartTime(time: String)

    val workEndTime: Flow<String>
    suspend fun setWorkEndTime(time: String)

    val customReminderText: Flow<String>
    suspend fun setCustomReminderText(text: String)

    val linkApp: Flow<String>
    suspend fun setLinkApp(app: String)
}

class CheckInSettingsRepositoryImpl(context: Context) : BaseSettingsRepositoryImpl(), CheckInSettingsRepository {
    companion object {
        private const val PREFERENCES_NAME = "check_in_prefs"
        private const val KEY_REMINDER_ENABLED = "reminder_enabled"
        private const val KEY_CHECK_IN_ONLY = "check_in_only"
        private const val KEY_WORK_START = "work_start_time"
        private const val KEY_WORK_END = "work_end_time"
        private const val KEY_REMINDER_TEXT = "custom_reminder_text"
        private const val KEY_LINK_APP = "link_app"
    }

    override val sharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private val _isReminderEnabled = MutableStateFlow(sharedPreferences.getBoolean(KEY_REMINDER_ENABLED, true))
    override val isReminderEnabled = _isReminderEnabled.asStateFlow()

    private val _checkInOnly = MutableStateFlow(sharedPreferences.getBoolean(KEY_CHECK_IN_ONLY, false))
    override val checkInOnly = _checkInOnly.asStateFlow()

    private val _workStartTime = MutableStateFlow(sharedPreferences.getString(KEY_WORK_START, "08:30") ?: "08:30")
    override val workStartTime = _workStartTime.asStateFlow()

    private val _workEndTime = MutableStateFlow(sharedPreferences.getString(KEY_WORK_END, "17:30") ?: "17:30")
    override val workEndTime = _workEndTime.asStateFlow()

    private val _customReminderText = MutableStateFlow(sharedPreferences.getString(KEY_REMINDER_TEXT, "上班时间请记得打卡") ?: "上班时间请记得打卡")
    override val customReminderText = _customReminderText.asStateFlow()

    private val _linkApp = MutableStateFlow(sharedPreferences.getString(KEY_LINK_APP, "none") ?: "none")
    override val linkApp = _linkApp.asStateFlow()

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener { prefs, key ->
            when (key) {
                KEY_REMINDER_ENABLED -> _isReminderEnabled.value = prefs.getBoolean(KEY_REMINDER_ENABLED, true)
                KEY_CHECK_IN_ONLY -> _checkInOnly.value = prefs.getBoolean(KEY_CHECK_IN_ONLY, false)
                KEY_WORK_START -> _workStartTime.value = prefs.getString(KEY_WORK_START, "08:30") ?: "08:30"
                KEY_WORK_END -> _workEndTime.value = prefs.getString(KEY_WORK_END, "17:30") ?: "17:30"
                KEY_REMINDER_TEXT -> _customReminderText.value = prefs.getString(KEY_REMINDER_TEXT, "上班时间请记得打卡") ?: "上班时间请记得打卡"
                KEY_LINK_APP -> _linkApp.value = prefs.getString(KEY_LINK_APP, "none") ?: "none"
            }
        }
    }

    // The in-memory StateFlow is updated synchronously in the setter (in
    // addition to the SharedPreferences listener), so callers that read
    // .first() right after a set - e.g. CheckInScheduler - always see the
    // new value instead of racing with the async listener callback.

    override suspend fun setReminderEnabled(enabled: Boolean) {
        _isReminderEnabled.value = enabled
        withContext(Dispatchers.IO) {
            sharedPreferences.edit { putBoolean(KEY_REMINDER_ENABLED, enabled) }
        }
    }

    override suspend fun setCheckInOnly(enabled: Boolean) {
        _checkInOnly.value = enabled
        withContext(Dispatchers.IO) {
            sharedPreferences.edit { putBoolean(KEY_CHECK_IN_ONLY, enabled) }
        }
    }

    override suspend fun setWorkStartTime(time: String) {
        _workStartTime.value = time
        withContext(Dispatchers.IO) {
            sharedPreferences.edit { putString(KEY_WORK_START, time) }
        }
    }

    override suspend fun setWorkEndTime(time: String) {
        _workEndTime.value = time
        withContext(Dispatchers.IO) {
            sharedPreferences.edit { putString(KEY_WORK_END, time) }
        }
    }

    override suspend fun setCustomReminderText(text: String) {
        _customReminderText.value = text
        withContext(Dispatchers.IO) {
            sharedPreferences.edit { putString(KEY_REMINDER_TEXT, text) }
        }
    }

    override suspend fun setLinkApp(app: String) {
        _linkApp.value = app
        withContext(Dispatchers.IO) {
            sharedPreferences.edit { putString(KEY_LINK_APP, app) }
        }
    }

    override suspend fun getBackup(): Map<String, String> = emptyMap()
    override suspend fun restoreBackup(settings: Map<String, String>) {}
}
