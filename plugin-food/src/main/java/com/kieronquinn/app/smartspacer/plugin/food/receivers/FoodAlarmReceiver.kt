package com.kieronquinn.app.smartspacer.plugin.food.receivers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.kieronquinn.app.smartspacer.plugin.food.R
import com.kieronquinn.app.smartspacer.plugin.food.data.FoodItemDao
import com.kieronquinn.app.smartspacer.plugin.food.providers.FoodProvider
import com.kieronquinn.app.smartspacer.plugin.food.repositories.FoodScheduler
import com.kieronquinn.app.smartspacer.plugin.shared.notifications.NotificationIds
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.verifySecurity
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.applySecurity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

/**
 * Fires when a food item enters its reminder window: shows the expiry notification (with
 * "标记已处理" and "稍后提醒" end actions) using the item's stable notification ID, refreshes the
 * Smartspacer target and re-schedules. This is a periodic static reminder, so it stays a regular
 * (non-promoted) notification per the official Live Update guidance.
 */
class FoodAlarmReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        private const val EXTRA_FOOD_ITEM_ID = "food_item_id"
        private const val CHANNEL_ID = "food_reminders"

        fun createIntent(context: Context, foodItemId: Int): Intent {
            return Intent(context, FoodAlarmReceiver::class.java).apply {
                putExtra(EXTRA_FOOD_ITEM_ID, foodItemId)
                applySecurity(context)
            }
        }
    }

    private val foodItemDao by inject<FoodItemDao>()
    private val foodScheduler by inject<FoodScheduler>()

    override fun onReceive(context: Context, intent: Intent) {
        intent.verifySecurity(context)
        val foodItemId = intent.getIntExtra(EXTRA_FOOD_ITEM_ID, -1)
        if (foodItemId == -1) return

        SmartspacerTargetProvider.notifyChange(context, FoodProvider::class.java)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val foodItem = foodItemDao.getById(foodItemId)
                if (foodItem != null) {
                    if (foodItem.enabled) {
                        showNotification(context, foodItemId, foodItem.name, foodItem.expiryDate)
                    }
                    foodScheduler.scheduleReminder(foodItem)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, foodItemId: Int, name: String, expiryDate: Long) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = NotificationIds.forEntity(NotificationIds.NAMESPACE_FOOD, foodItemId.toLong())

        val now = System.currentTimeMillis()
        val content = if (expiryDate <= now) {
            context.getString(R.string.notification_food_expired_content, name)
        } else {
            val days = TimeUnit.MILLISECONDS.toDays(expiryDate - now)
            context.getString(R.string.notification_food_expiring_content, name, days + 1)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_kitchen)
            .setContentTitle(context.getString(R.string.notification_food_title))
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(Notification.CATEGORY_REMINDER)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_kitchen,
                context.getString(R.string.notification_action_handled),
                actionIntent(context, foodItemId, notificationId, FoodActionReceiver.ACTION_HANDLED)
            )
            .addAction(
                R.drawable.ic_kitchen,
                context.getString(R.string.notification_action_snooze),
                actionIntent(context, foodItemId, notificationId, FoodActionReceiver.ACTION_SNOOZE)
            )
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun actionIntent(
        context: Context,
        foodItemId: Int,
        notificationId: Int,
        action: String
    ): PendingIntent {
        val intent = Intent(context, FoodActionReceiver::class.java).apply {
            this.action = action
            putExtra(FoodActionReceiver.EXTRA_FOOD_ITEM_ID, foodItemId)
            putExtra(FoodActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        return PendingIntent.getBroadcast(
            context,
            NotificationIds.forEntity("food_action_$action", foodItemId.toLong()),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
