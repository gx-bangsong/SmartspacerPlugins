package com.kieronquinn.app.smartspacer.plugin.foodreminder.provider
import android.content.Context
import android.content.ComponentName
import com.kieronquinn.smartspacer.sdk.feature.sublist.SubListTemplateData
import com.kieronquinn.smartspacer.sdk.feature.sublist.SubListTemplateData.SubListItem
import com.kieronquinn.smartspacer.sdk.feature.sublist.FEATURE_SUB_LIST
import com.kieronquinn.smartspacer.sdk.feature.sublist.Text
import com.kieronquinn.app.smartspacer.plugin.foodreminder.FoodReminderSettings
import com.kieronquinn.app.smartspacer.plugin.foodreminder.data.FoodItemRepository
import com.kieronquinn.smartspacer.sdk.SmartspacerProvider
import com.kieronquinn.smartspacer.sdk.model.SmartspaceTarget
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.module.Module
import java.util.concurrent.TimeUnit

class FoodReminderProvider : SmartspacerProvider(), KoinComponent {

    private val repository: FoodItemRepository by inject()
    private val settings: FoodReminderSettings by inject()
    private val context: Context by inject() // 注入 context

    override suspend fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        val foodItems = repository.allFoodItems.first()
        if (foodItems.isEmpty()) return emptyList()

        val reminderTimeframe = settings.reminderTimeframe
        val expiringItems = foodItems.filter {
            val diff = it.expiryDate - System.currentTimeMillis()
            val days = TimeUnit.MILLISECONDS.toDays(diff)
            days in 0..reminderTimeframe
        }
        if (expiringItems.isEmpty()) return emptyList()

        val subListItems = expiringItems.map {
            SubListItem(
                text = "${it.name} - Expires in ${TimeUnit.MILLISECONDS.toDays(it.expiryDate - System.currentTimeMillis())} days",
                tapAction = null
            )
        }

        return listOf(
            SmartspaceTarget(
                smartspaceTargetId = "food_reminder_list",
                featureType = FEATURE_SUB_LIST,
                componentName = ComponentName(context.packageName, javaClass.name),
                templateData = SubListTemplateData(
                    title = "Expiring Food",
                    subListItems = subListItems
                )
            )
        )
    }
}
