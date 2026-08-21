package com.kieronquinn.app.smartspacer.plugin.travel.repositories

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import com.kieronquinn.app.smartspacer.plugin.travel.data.TravelInfoItem
import com.kieronquinn.app.smartspacer.plugin.travel.data.TravelInfoDao
import com.kieronquinn.app.smartspacer.plugin.travel.receivers.TravelAlarmReceiver
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.PendingIntent_MUTABLE_FLAGS

interface TravelScheduler {
    fun hasPermission(): Boolean
    fun scheduleReminder(item: TravelInfoItem)
    fun cancelReminder(itemId: Int)
    suspend fun rescheduleAll()
}

class TravelSchedulerImpl(
    private val context: Context,
    private val travelInfoDao: TravelInfoDao
) : TravelScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun hasPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return alarmManager.canScheduleExactAlarms()
    }

    override fun scheduleReminder(item: TravelInfoItem) {
        cancelReminder(item.id)

        if (!hasPermission() || item.isUsed) {
            return
        }

        val now = System.currentTimeMillis()
        // 提前 30 分钟提醒
        val reminderTime = item.departureTime - 30L * 60 * 1000

        if (reminderTime > now) {
            val intent = TravelAlarmReceiver.createIntent(context, item.id)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                item.id,
                intent,
                PendingIntent_MUTABLE_FLAGS
            )
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent
            )
        }
    }

    override fun cancelReminder(itemId: Int) {
        val intent = TravelAlarmReceiver.createIntent(context, itemId)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            itemId,
            intent,
            PendingIntent_MUTABLE_FLAGS or PendingIntent.FLAG_NO_CREATE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    override suspend fun rescheduleAll() {
        val now = System.currentTimeMillis()
        val trips = travelInfoDao.getUnusedTrips(now)
        for (trip in trips) {
            scheduleReminder(trip)
        }
    }
}
