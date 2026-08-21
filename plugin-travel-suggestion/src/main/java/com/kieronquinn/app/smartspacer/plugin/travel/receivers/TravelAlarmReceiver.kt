package com.kieronquinn.app.smartspacer.plugin.travel.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.travel.data.TravelInfoDao
import com.kieronquinn.app.smartspacer.plugin.travel.notifications.TravelNotificationController
import com.kieronquinn.app.smartspacer.plugin.travel.repositories.TravelScheduler
import com.kieronquinn.app.smartspacer.plugin.travel.repositories.TravelSuppressionRepository
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.verifySecurity
import com.kieronquinn.app.smartspacer.sdk.utils.applySecurity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Fires at T-30 (departure window): upgrades the trip's existing notification (posted by
 * [TravelSmsReceiver] or the share flow) in place to a promoted Live Update with a system
 * countdown, and schedules the post-departure cleanup alarm. Also handles that cleanup alarm
 * (cancelling the Live Update after the departure + grace period).
 */
class TravelAlarmReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        private const val EXTRA_TRAVEL_ITEM_ID = "travel_item_id"
        const val EXTRA_ACTION = "extra_action"
        const val ACTION_REMINDER = "reminder"
        const val ACTION_CLEANUP = "cleanup"

        fun createIntent(context: Context, travelItemId: Int, action: String = ACTION_REMINDER): Intent {
            return Intent(context, TravelAlarmReceiver::class.java).apply {
                putExtra(EXTRA_TRAVEL_ITEM_ID, travelItemId)
                putExtra(EXTRA_ACTION, action)
                applySecurity(context)
            }
        }
    }

    private val travelInfoDao by inject<TravelInfoDao>()
    private val travelScheduler by inject<TravelScheduler>()
    private val suppressionRepository by inject<TravelSuppressionRepository>()
    private val notificationController by inject<TravelNotificationController>()

    override fun onReceive(context: Context, intent: Intent) {
        intent.verifySecurity(context)
        val travelItemId = intent.getIntExtra(EXTRA_TRAVEL_ITEM_ID, -1)
        if (travelItemId == -1) return
        val action = intent.getStringExtra(EXTRA_ACTION) ?: ACTION_REMINDER

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val item = travelInfoDao.getById(travelItemId)
                if (action == ACTION_CLEANUP) {
                    // Departure + grace period passed: end the Live Update, never leave a
                    // permanent ongoing notification behind.
                    if (item != null) {
                        notificationController.cancelTrip(item.id)
                        suppressionRepository.clearForTrip(item.id)
                    }
                    return@launch
                }

                if (item != null && !item.isUsed && !suppressionRepository.isSuppressed(item.id)) {
                    notificationController.postTripLiveUpdate(item)
                    // Reschedule keeps the cleanup alarm in sync; the T-30 reminder itself is in
                    // the past and will simply not be re-scheduled.
                    travelScheduler.scheduleReminder(item)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
