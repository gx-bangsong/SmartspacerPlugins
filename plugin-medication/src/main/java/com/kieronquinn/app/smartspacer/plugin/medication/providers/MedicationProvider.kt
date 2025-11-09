package com.kieronquinn.app.smartspacer.plugin.medication.providers

import com.kieronquinn.app.smartspacer.plugin.medication.R
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider

class MedicationProvider : SmartspacerTargetProvider() {

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        // Logic to create and return the medication reminder target will be added here
        return emptyList()
    }

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            label = "Medication Reminder",
            description = "A medication reminder",
            icon = android.graphics.drawable.Icon.createWithResource(context, R.drawable.ic_launcher_foreground)
        )
    }

    override fun onDismiss(smartspacerId: String, targetId: String): Boolean {
        return false
    }

}