package com.kieronquinn.app.smartspacer.plugin.waterreminder.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kieronquinn.app.smartspacer.plugin.waterreminder.data.WaterReminderSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.waterreminder.providers.WaterReminderProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WaterActionReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        const val TAG = "WaterActionReceiver"
        const val ACTION_LOG_WATER = "com.kieronquinn.app.smartspacer.plugin.waterreminder.LOG_WATER"
    }

    private val settingsRepository: WaterReminderSettingsRepository by inject()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_LOG_WATER) return

        val pendingResult = goAsync()

        scope.launch {
            try {
                Log.d(TAG, "Received log water action")
                val currentProgress = settingsRepository.currentProgressCups.get()
                settingsRepository.currentProgressCups.set(currentProgress + 1)
                notifyProvider(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun notifyProvider(context: Context) {
        val intent = Intent(context, WaterReminderProvider::class.java).apply {
            action = WaterReminderProvider.ACTION_REFRESH
        }
        context.sendBroadcast(intent)
        Log.d(TAG, "Sent refresh broadcast to provider.")
    }
}