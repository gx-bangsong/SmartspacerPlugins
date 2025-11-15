package com.kieronquinn.app.smartspacer.plugin.food.providers

import android.content.ComponentName
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.food.R
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget

import com.kieronquinn.app.smartspacer.plugin.food.data.FoodItemDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class FoodProvider : SmartspacerTargetProvider(), KoinComponent {

    private val foodItemDao by inject<FoodItemDao>()

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        val foodItems = runBlocking { foodItemDao.getAll().first() }
        val now = System.currentTimeMillis()

        return foodItems
            .filter { it.enabled }
            .mapNotNull { foodItem ->
                val expiresInMillis = foodItem.expiryDate - now
                if (expiresInMillis <= 0) return@mapNotNull null

                val reminderThreshold = TimeUnit.DAYS.toMillis(foodItem.reminderOffsetDays.toLong())
                if (expiresInMillis > reminderThreshold) return@mapNotNull null

                val expiresInDays = TimeUnit.MILLISECONDS.toDays(expiresInMillis)
                SmartspaceTarget(
                    smartspaceTargetId = "food_${foodItem.id}",
                    headerAction = SmartspaceAction(
                        id = "food_header_${foodItem.id}",
                        title = "${foodItem.name} - Expires in $expiresInDays days",
                        intent = Intent(context, com.kieronquinn.app.smartspacer.plugin.food.ui.activities.SettingsActivity::class.java)
                    ),
                    featureType = SmartspaceTarget.FEATURE_REMINDER,
                    componentName = ComponentName(context, FoodProvider::class.java)
                )
            }
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