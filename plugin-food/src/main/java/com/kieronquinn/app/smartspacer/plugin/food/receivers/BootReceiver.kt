package com.kieronquinn.app.smartspacer.plugin.food.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.food.repositories.FoodScheduler
import com.kieronquinn.app.smartspacer.plugin.food.work.FoodWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * BootReceiver 负责在设备启动时重新调度 FoodWorker 任务和闹钟。
 */
class BootReceiver : BroadcastReceiver(), KoinComponent {

    private val foodScheduler by inject<FoodScheduler>()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            FoodWorker.enqueuePeriodic(context)

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    foodScheduler.rescheduleAll()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

}
