package com.kieronquinn.app.smartspacer.plugin.food.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.kieronquinn.app.smartspacer.plugin.food.data.FoodItemDao
import com.kieronquinn.app.smartspacer.plugin.food.providers.FoodProvider
import com.kieronquinn.app.smartspacer.plugin.food.repositories.FoodScheduler
import com.kieronquinn.app.smartspacer.plugin.food.work.FoodWorker
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Calendar

/**
 * Notification actions for the expiry reminder:
 *  - [ACTION_HANDLED]: marks the item as handled (disables its reminders and hides it from the
 *    Smartspacer target) and cancels the notification — the reminder's end condition;
 *  - [ACTION_SNOOZE]: re-reminds tomorrow morning at 08:00 and cancels the current notification.
 */
class FoodActionReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        const val ACTION_HANDLED = "com.kieronquinn.app.smartspacer.plugin.food.ACTION_HANDLED"
        const val ACTION_SNOOZE = "com.kieronquinn.app.smartspacer.plugin.food.ACTION_SNOOZE"
        const val EXTRA_FOOD_ITEM_ID = "food_item_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }

    private val foodItemDao by inject<FoodItemDao>()
    private val foodScheduler by inject<FoodScheduler>()

    override fun onReceive(context: Context, intent: Intent) {
        val foodItemId = intent.getIntExtra(EXTRA_FOOD_ITEM_ID, -1)
        if (foodItemId == -1) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val foodItem = foodItemDao.getById(foodItemId)
                if (foodItem != null) {
                    when (intent.action) {
                        ACTION_HANDLED -> {
                            foodItemDao.update(foodItem.copy(enabled = false))
                            foodScheduler.cancelReminder(foodItem.id)
                        }
                        ACTION_SNOOZE -> {
                            val calendar = Calendar.getInstance().apply {
                                add(Calendar.DAY_OF_YEAR, 1)
                                set(Calendar.HOUR_OF_DAY, 8)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            // 直接调度明天的提醒；foodItem 的 reminderStart 仍可使用
                            com.kieronquinn.app.smartspacer.plugin.food.receivers.FoodSnoozeScheduler.schedule(
                                context, foodItem.id, calendar.timeInMillis
                            )
                        }
                    }
                    SmartspacerTargetProvider.notifyChange(context, FoodProvider::class.java)
                    FoodWorker.enqueueImmediate(context)
                }

                val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
                if (notificationId != -1) {
                    NotificationManagerCompat.from(context).cancel(notificationId)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}

/**
 * Schedules a one-off snoozed expiry reminder for [FoodAlarmReceiver].
 */
object FoodSnoozeScheduler {
    fun schedule(context: Context, foodItemId: Int, triggerAtMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val intent = FoodAlarmReceiver.createIntent(context, foodItemId)
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            com.kieronquinn.app.smartspacer.plugin.shared.notifications.NotificationIds.forEntity(
                "food_snooze", foodItemId.toLong()
            ),
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            android.app.AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }
}
