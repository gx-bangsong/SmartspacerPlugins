package com.kieronquinn.app.smartspacer.plugin.shared.permissions

import android.content.Context
import com.kieronquinn.app.shared.R

object PermissionOnboardingSettings {

    fun subtitle(context: Context, config: PluginPermissionConfig): String {
        val missing = PermissionStatusRepository(context).missingCount(config.capabilities)
        return if (missing == 0) {
            context.getString(R.string.permission_onboarding_settings_all_ok)
        } else {
            context.getString(R.string.permission_onboarding_settings_missing, missing)
        }
    }
}
