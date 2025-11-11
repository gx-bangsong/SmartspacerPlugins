package com.kieronquinn.app.smartspacer.plugin.food.providers

import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.food.R
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget

class FoodProvider : SmartspacerTargetProvider() {

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        // Logic to create and return the food shelf life target will be added here
        return emptyList()
    }

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            label = "Food Shelf Life Reminder",
            description = "Track the shelf life of your food",
            icon = android.graphics.drawable.Icon.createWithResource(context, R.drawable.ic_launcher_foreground),
            configActivity = Intent(context, com.kieronquinn.app.smartspacer.plugin.food.ui.activities.SettingsActivity::class.java)
        )
    }

    override fun onDismiss(smartspacerId: String, targetId: String): Boolean {
        return false
    }

}