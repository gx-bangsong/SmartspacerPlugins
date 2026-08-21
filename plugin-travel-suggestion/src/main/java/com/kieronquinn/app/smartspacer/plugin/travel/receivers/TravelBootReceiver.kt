package com.kieronquinn.app.smartspacer.plugin.travel.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.travel.data.TravelInfoDao
import com.kieronquinn.app.smartspacer.plugin.travel.notifications.TravelNotificationController
import com.kieronquinn.app.smartspacer.plugin.travel.repositories.TravelScheduler
import com.kieronquinn.app.smartspacer.plugin.travel.repositories.TravelShareOperationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Re-schedules travel reminders after reboot / timezone change / package update and cancels
 * stale Live Updates for trips whose departure (+ grace period) has passed. Because every
 * scheduling operation first cancels the previous alarm and the notification IDs are stable per
 * trip, this never produces duplicate notifications.
 */
class TravelBootReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        private const val EXTRA_ACTION = TravelAlarmReceiver.EXTRA_ACTION
    }

    private val travelInfoDao by inject<TravelInfoDao>()
    private val travelScheduler by inject<TravelScheduler>()
    private val opRepository by inject<TravelShareOperationRepository>()
    private val notificationController by inject<TravelNotificationController>()

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_TIMEZONE_CHANGED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val now = System.currentTimeMillis()

                // Cancel Live Updates for trips that have already departed (+ grace period).
                val allUnused = travelInfoDao.getUnusedAll()
                allUnused.filter { it.departureTime + TravelNotificationController.GRACE_PERIOD_MS < now }
                    .forEach { notificationController.cancelTrip(it.id) }

                travelScheduler.rescheduleAll()
                opRepository.pruneExpired(now)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
