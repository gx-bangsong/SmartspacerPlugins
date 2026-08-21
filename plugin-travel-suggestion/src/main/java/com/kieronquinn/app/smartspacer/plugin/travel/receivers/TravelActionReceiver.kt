package com.kieronquinn.app.smartspacer.plugin.travel.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.travel.data.TravelInfoDao
import com.kieronquinn.app.smartspacer.plugin.travel.notifications.TravelNotificationController
import com.kieronquinn.app.smartspacer.plugin.travel.providers.TravelTargetProvider
import com.kieronquinn.app.smartspacer.plugin.travel.repositories.TravelScheduler
import com.kieronquinn.app.smartspacer.plugin.travel.repositories.TravelSuppressionRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Handles the Live Update actions: "标记已出行" (mark as used → cancel the Live Update and the
 * reminder) and user dismissal (remembered so rescheduling never re-posts the notification).
 */
class TravelActionReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        const val ACTION_MARK_USED = "com.kieronquinn.app.smartspacer.plugin.travel.ACTION_MARK_USED"
        const val ACTION_DISMISSED = "com.kieronquinn.app.smartspacer.plugin.travel.ACTION_DISMISSED"
        const val EXTRA_TRIP_ID = "extra_trip_id"
    }

    private val travelInfoDao by inject<TravelInfoDao>()
    private val travelScheduler by inject<TravelScheduler>()
    private val suppressionRepository by inject<TravelSuppressionRepository>()
    private val notificationController by inject<TravelNotificationController>()

    override fun onReceive(context: Context, intent: Intent) {
        val tripId = intent.getIntExtra(EXTRA_TRIP_ID, -1)
        if (tripId == -1) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_MARK_USED -> {
                        val item = travelInfoDao.getById(tripId)
                        if (item != null && !item.isUsed) {
                            travelInfoDao.update(item.copy(isUsed = true))
                            travelScheduler.cancelReminder(tripId)
                            suppressionRepository.clearForTrip(tripId)
                            notificationController.cancelTrip(tripId)
                            SmartspacerTargetProvider.notifyChange(context, TravelTargetProvider::class.java)
                        }
                    }
                    ACTION_DISMISSED -> {
                        // Remember the dismissal so the notification is not re-posted by later
                        // rescheduling (reboot / timezone change / re-scan).
                        suppressionRepository.suppressTrip(tripId)
                        notificationController.cancelTrip(tripId)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
