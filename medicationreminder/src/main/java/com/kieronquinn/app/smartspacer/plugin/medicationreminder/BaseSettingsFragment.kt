package com.kieronquinn.app.smartspacer.plugin.medicationreminder

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.R
import org.koin.android.ext.android.inject

abstract class BaseSettingsFragment: PreferenceFragmentCompat() {

    abstract val androidXMl: Int

    val settings by inject<MedicationReminderSettings>()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(androidXMl, rootKey)
    }

}