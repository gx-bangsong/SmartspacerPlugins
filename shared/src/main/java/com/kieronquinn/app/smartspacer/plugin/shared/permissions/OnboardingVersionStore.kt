package com.kieronquinn.app.smartspacer.plugin.shared.permissions

import android.content.Context
import android.content.SharedPreferences

/**
 * Persists the last onboarding version that was presented to the user. This is intentionally
 * separate from live permission state — "already shown" must never be inferred from grants.
 */
interface OnboardingVersionStore {
    var shownVersion: Int
}

class InMemoryOnboardingVersionStore(override var shownVersion: Int = 0) : OnboardingVersionStore

class SharedPreferencesOnboardingVersionStore(
    private val prefs: SharedPreferences
) : OnboardingVersionStore {

    override var shownVersion: Int
        get() = prefs.getInt(KEY_PERMISSION_ONBOARDING_VERSION, 0)
        set(value) {
            prefs.edit().putInt(KEY_PERMISSION_ONBOARDING_VERSION, value).commit()
        }

    companion object {
        const val PREFS_NAME = "plugin_permission_onboarding"
        const val KEY_PERMISSION_ONBOARDING_VERSION = "permission_onboarding_version"

        fun from(context: Context): SharedPreferencesOnboardingVersionStore {
            return SharedPreferencesOnboardingVersionStore(
                context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            )
        }
    }
}
