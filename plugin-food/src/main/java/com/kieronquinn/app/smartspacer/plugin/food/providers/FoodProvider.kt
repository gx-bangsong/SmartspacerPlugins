package com.kieronquinn.app.smartspacer.plugin.food.providers

import android.content.ComponentName
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.food.R
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon as SmartspaceIcon
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate
import android.graphics.drawable.Icon as AndroidIcon

import com.kieronquinn.app.smartspacer.plugin.food.data.FoodItemDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class FoodProvider : SmartspacerTargetProvider(), KoinComponent {

    private val foodItemDao by inject<FoodItemDao>()

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        val context = this.context ?: return emptyList()
        val foodItems = runBlocking { foodItemDao.getAll().first() }
        val now = System.currentTimeMillis()

        return foodItems
            .filter { it.enabled }
            .map { foodItem ->
                val expiresInMillis = foodItem.expiryDate - now
                val title = if (expiresInMillis <= 0) {
                    "${foodItem.name} - Expired"
                } else {
                    val expiresInDays = TimeUnit.MILLISECONDS.toDays(expiresInMillis)
                    if (expiresInDays > 0) {
                        "${foodItem.name} - Expires in $expiresInDays days"
                    } else {
                        val expiresInHours = TimeUnit.MILLISECONDS.toHours(expiresInMillis)
                        "${foodItem.name} - Expires in $expiresInHours hours"
                    }
                }

                val intent = Intent(context, com.kieronquinn.app.smartspacer.plugin.food.ui.activities.SettingsActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                TargetTemplate.Basic(
                    id = "food_${foodItem.id}",
                    componentName = ComponentName(context, FoodProvider::class.java),
                    featureType = SmartspaceTarget.FEATURE_REMINDER,
                    title = Text(title),
                    subtitle = Text(""),
                    icon = SmartspaceIcon(AndroidIcon.createWithResource(context, R.mipmap.ic_launcher), shouldTint = false),
                    onClick = TapAction(intent = intent)
                ).create()
            }
    }

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            label = "Food Shelf Life Reminder",
            description = "Track the shelf life of your food",
            icon = AndroidIcon.createWithResource(context, R.mipmap.ic_launcher),
            configActivity = Intent(context, com.kieronquinn.app.smartspacer.plugin.food.ui.activities.SettingsActivity::class.java)
        )
    }

    override fun onDismiss(smartspacerId: String, targetId: String): Boolean {
        return false
    }

}
