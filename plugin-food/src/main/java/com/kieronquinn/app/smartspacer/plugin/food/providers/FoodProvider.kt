package com.kieronquinn.app.smartspacer.plugin.food.providers

import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.food.ui.activities.SettingsActivity
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction

class FoodProvider : SmartspacerTargetProvider() {

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        // Logic to create and return the food shelf life target will be added here
        return emptyList()
    }

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            "Food Shelf Life",
            "Reminds you about food shelf life",
            Intent(this, SettingsActivity::class.java),
            "ic_food"
        )
    }

}