package com.kieronquinn.app.smartspacer.plugin.water.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.kieronquinn.app.smartspacer.plugin.water.data.DrinkHistory
import com.kieronquinn.app.smartspacer.plugin.water.data.DrinkHistoryDao
import com.kieronquinn.app.smartspacer.plugin.water.providers.WaterProvider
import com.kieronquinn.app.smartspacer.plugin.water.repositories.WaterDataRepository
import com.kieronquinn.app.smartspacer.plugin.water.scheduling.WaterScheduler
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Notification actions:
 *  - [ACTION_DRINK]: records a cup of water, refreshes the Smartspacer target and cancels the
 *    reminder notification (the reminder's end condition).
 *  - [ACTION_SNOOZE]: schedules a new reminder `snoozeMinutes` from now and cancels the current
 *    notification, replacing it with the snoozed one.
 */
class WaterActionReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        const val ACTION_DRINK = "com.kieronquinn.app.smartspacer.plugin.water.ACTION_DRINK"
        const val ACTION_SNOOZE = "com.kieronquinn.app.smartspacer.plugin.water.ACTION_SNOOZE"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_AMOUNT = "extra_amount"
    }

    private val drinkHistoryDao by inject<DrinkHistoryDao>()
    private val waterDataRepository by inject<WaterDataRepository>()
    private val waterScheduler by inject<WaterScheduler>()

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        if (notificationId != -1) {
            NotificationManagerCompat.from(context).cancel(notificationId)
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_DRINK -> {
                        val amount = intent.getIntExtra(EXTRA_AMOUNT, waterDataRepository.cupMl)
                            .takeIf { it > 0 } ?: waterDataRepository.cupMl
                        drinkHistoryDao.insert(
                            DrinkHistory(timestamp = System.currentTimeMillis(), amount = amount)
                        )
                    }
                    ACTION_SNOOZE -> {
                        val snoozeTime = System.currentTimeMillis() +
                            waterDataRepository.snoozeMinutes * 60 * 1000L
                        waterScheduler.scheduleSnooze(context, snoozeTime)
                    }
                }
                SmartspacerTargetProvider.notifyChange(
                    context, WaterProvider::class.java
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}
