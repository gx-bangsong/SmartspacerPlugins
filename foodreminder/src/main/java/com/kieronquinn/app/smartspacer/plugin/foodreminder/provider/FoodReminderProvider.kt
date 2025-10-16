package com.kieronquinn.app.smartspacer.plugin.foodreminder.provider

import android.content.Context
import android.content.ComponentName
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import com.kieronquinn.smartspacer.sdk.SmartspacerPlugin
import com.kieronquinn.smartspacer.sdk.SmartspacerProvider
import com.kieronquinn.smartspacer.sdk.feature.sublist.SubListItem
import com.kieronquinn.smartspacer.sdk.feature.sublist.FEATURE_SUB_LIST
import java.util.concurrent.TimeUnit

class FoodReminderProvider : com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerProvider() {

    private val repository: FoodItemRepository by inject()
    private val settings: com.kieronquinn.app.smartspacer.plugin.foodreminder.FoodReminderSettings by inject()

    override suspend fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        val foodItems = repository.allFoodItems.first()
        if (foodItems.isEmpty()) {
            return emptyList()
        }

        val reminderTimeframe = settings.reminderTimeframe
        val expiringItems = foodItems.filter {
            val diff = it.expiryDate - System.currentTimeMillis()
            val days = TimeUnit.MILLISECONDS.toDays(diff)
            days in 0..reminderTimeframe
        }

        if (expiringItems.isEmpty()) {
            return emptyList()
        }

        val subListItems = expiringItems.map {
            SubListTemplateData.SubListItem(
                text = Text("${it.name} - Expires in ${TimeUnit.MILLISECONDS.toDays(it.expiryDate - System.currentTimeMillis())} days"),
                tapAction = null
            )
        }

        return listOf(
            SmartspaceTarget(
                smartspaceTargetId = "food_reminder_list",
                featureType = SmartspaceTarget.FEATURE_SUB_LIST,
                componentName = componentName,
                templateData = SubListTemplateData(
                    title = Text("Expiring Food"),
                    subListTexts = subListItems
                )
            )
        )
    }
}