package com.kieronquinn.app.smartspacer.plugin.medicationreminder.data

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.BaseSettingsRepositoryImpl
import kotlinx.coroutines.flow.MutableSharedFlow

class SnoozeRepository(
    context: Context,
    val onChange: MutableSharedFlow<String> = MutableSharedFlow(replay = 1)
) : BaseSettingsRepositoryImpl() {

    companion object {
        private const val PREFERENCES_NAME = "medication_snooze_settings"
    }

    override val sharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun isSnoozed(medicationId: Int): Boolean {
        return sharedPreferences.getLong(medicationId.toString(), 0) > System.currentTimeMillis()
    }

    fun snooze(medicationId: Int, durationMinutes: Long) {
        val snoozeUntil = System.currentTimeMillis() + durationMinutes * 60 * 1000
        sharedPreferences.edit().putLong(medicationId.toString(), snoozeUntil).apply()
    }

    fun clearSnooze(medicationId: Int) {
        sharedPreferences.edit().remove(medicationId.toString()).apply()
    }
}