package com.kieronquinn.app.smartspacer.plugin.water.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.kieronquinn.app.smartspacer.plugin.water.repositories.WaterDataRepository
import com.kieronquinn.app.smartspacer.plugin.water.scheduling.WaterScheduler
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate

class WaterActionReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        const val ACTION_DRINK = "com.kieronquinn.app.smartspacer.plugin.water.ACTION_DRINK"
        const val ACTION_SNOOZE = "com.kieronquinn.app.smartspacer.plugin.water.ACTION_SNOOZE"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }

    private val waterDataRepository by inject<WaterDataRepository>()
    private val waterScheduler by inject<WaterScheduler>()

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        if (notificationId != -1) {
            NotificationManagerCompat.from(context).cancel(notificationId)
        }

        when (intent.action) {
            ACTION_DRINK -> {
                // This is now handled by the dialog
            }
            ACTION_SNOOZE -> {
                // This is now handled by the dialog
            }
        }
        SmartspacerTargetProvider.notifyChange(context, com.kieronquinn.app.smartspacer.plugin.water.providers.WaterProvider::class.java)
    }
}
