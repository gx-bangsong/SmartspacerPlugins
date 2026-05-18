package com.kieronquinn.app.smartspacer.plugin.food.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.food.work.FoodWorker

/**
 * BootReceiver 负责在设备启动时重新调度 FoodWorker 任务。
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            FoodWorker.enqueuePeriodic(context)
        }
    }

}
