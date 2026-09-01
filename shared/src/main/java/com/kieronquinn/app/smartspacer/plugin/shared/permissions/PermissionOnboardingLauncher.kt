package com.kieronquinn.app.smartspacer.plugin.shared.permissions

import android.app.Activity
import android.content.Context
import android.content.Intent

object PermissionOnboardingLauncher {

    fun coordinator(context: Context, config: PluginPermissionConfig): PermissionOnboardingCoordinator {
        return PermissionOnboardingCoordinator(
            SharedPreferencesOnboardingVersionStore.from(context),
            config
        )
    }

    /**
     * First-launch entry point. Marks the current onboarding version as shown *before* starting
     * the wizard so skip / deny / process death cannot re-trigger auto-show.
     */
    fun maybeLaunch(activity: Activity, config: PluginPermissionConfig) {
        val coordinator = coordinator(activity, config)
        if (!coordinator.shouldAutoShow()) return
        coordinator.markShown()
        activity.startActivity(createIntent(activity, config))
    }

    /** Settings-page "re-run permission wizard". Does not change the stored version. */
    fun launch(context: Context, config: PluginPermissionConfig) {
        val intent = createIntent(context, config)
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun createIntent(context: Context, config: PluginPermissionConfig): Intent {
        return PermissionOnboardingActivity.createIntent(context, config)
    }
}
