package com.kieronquinn.app.smartspacer.plugin.foodreminder.providers

import android.content.Intent
import android.util.Log
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.SubcardTemplateData
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.plugin.foodreminder.data.FoodItemRepository
import com.kieronquinn.app.smartspacer.plugin.foodreminder.data.FoodReminderSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.foodreminder.ui.settings.FoodReminderSettingsActivity
import kotlinx.coroutines.flow.first
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class FoodReminderProvider : SmartspacerTargetProvider() {

    companion object {
        private const val TAG = "FoodReminderProvider"
        const val ACTION_REFRESH = "com.kieronquinn.app.smartspacer.plugin.foodreminder.REFRESH"
    }

    private val repository: FoodItemRepository by inject()
    private val settingsRepository: FoodReminderSettingsRepository by inject()

    override suspend fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        val leadTimeDays = settingsRepository.reminderLeadTimeDays.get()
        val expiringItems = repository.getFoodItems().first().filter {
            val daysUntilExpiry = TimeUnit.MILLISECONDS.toDays(it.expiryDate - System.currentTimeMillis())
            daysUntilExpiry in 0..leadTimeDays
        }

        if (expiringItems.isEmpty()) {
            Log.d(TAG, "No expiring items to show")
            return emptyList()
        }

        val settingsIntent = Intent(context, FoodReminderSettingsActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val subcards = expiringItems.map { item ->
            val daysUntilExpiry = TimeUnit.MILLISECONDS.toDays(item.expiryDate - System.currentTimeMillis())
            val expiryText = when {
                daysUntilExpiry < 0 -> "Expired"
                daysUntilExpiry == 0L -> "Expires today"
                daysUntilExpiry == 1L -> "Expires in 1 day"
                else -> "Expires in $daysUntilExpiry days"
            }
            SubcardTemplateData.SubcardInfo(
                text = Text("${item.name} - $expiryText"),
                tapAction = TapAction(intent = settingsIntent)
            )
        }

        val target = SmartspaceTarget(
            id = "food_reminder_target",
            componentName = componentName,
            smartspaceTargetId = "food_reminder_target",
            templateData = SubcardTemplateData(
                subcardInfos = subcards,
                subcardAction = TapAction(intent = settingsIntent)
            ),
            headerAction = SmartspaceAction(
                id = "food_reminder_header",
                intent = settingsIntent,
                title = "Food Reminders"
            )
        )

        Log.d(TAG, "Providing ${subcards.size} items to target")
        return listOf(target)
    }

    override fun onReceive(intent: Intent) {
        super.onReceive(intent)
        if (intent.action == ACTION_REFRESH) {
            Log.d(TAG, "Received refresh broadcast, notifying change.")
            notifyChange()
        }
    }
}