package com.kieronquinn.app.smartspacer.plugin.parcel.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.parcel.data.ParcelDao
import com.kieronquinn.app.smartspacer.plugin.parcel.notifications.ParcelNotificationController
import com.kieronquinn.app.smartspacer.plugin.parcel.notifications.ParcelSuppressionRepository
import com.kieronquinn.app.smartspacer.plugin.parcel.providers.ParcelTargetProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Handles parcel notification actions. "已取件" must atomically update Room, refresh Smartspacer
 * and cancel the notification; "停止实时显示 / 用户移除" records the dismissal so nothing re-posts
 * it later.
 */
class ParcelActionReceiver : BroadcastReceiver(), KoinComponent {
    private val parcelDao by inject<ParcelDao>()
    private val notificationController by inject<ParcelNotificationController>()
    private val suppressionRepository by inject<ParcelSuppressionRepository>()

    override fun onReceive(context: Context, intent: Intent) {
        val parcelId = intent.getLongExtra(EXTRA_PARCEL_ID, -1L)
        if (parcelId == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_MARK_AS_PICKED_UP -> {
                        parcelDao.markAsPickedUp(parcelId)
                        suppressionRepository.clearForParcel(parcelId)
                        notificationController.cancelParcel(parcelId)
                        SmartspacerTargetProvider.notifyChange(context, ParcelTargetProvider::class.java)
                    }
                    ACTION_UNPIN -> {
                        // User explicitly stopped the live display: remember it and cancel.
                        suppressionRepository.suppressParcel(parcelId)
                        notificationController.cancelParcel(parcelId)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_MARK_AS_PICKED_UP = "com.kieronquinn.app.smartspacer.plugin.parcel.ACTION_MARK_AS_PICKED_UP"
        const val ACTION_UNPIN = "com.kieronquinn.app.smartspacer.plugin.parcel.ACTION_UNPIN"
        const val EXTRA_PARCEL_ID = "extra_parcel_id"
    }
}
