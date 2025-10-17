package com.kieronquinn.app.smartspacer.plugin.foodreminder

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.kieronquinn.app.smartspacer.plugin.foodreminder.R
import org.koin.android.ext.android.inject

abstract class BaseSettingsFragment: PreferenceFragmentCompat() {

    abstract val androidXMl: Int

    val settings by inject<FoodReminderSettings>()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(androidXMl, rootKey)
    }

}