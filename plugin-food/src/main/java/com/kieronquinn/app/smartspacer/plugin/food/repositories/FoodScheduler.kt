package com.kieronquinn.app.smartspacer.plugin.food.repositories

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import com.kieronquinn.app.smartspacer.plugin.food.data.FoodItem
import com.kieronquinn.app.smartspacer.plugin.food.data.FoodItemDao
import com.kieronquinn.app.smartspacer.plugin.food.receivers.FoodAlarmReceiver
import com.kieronquinn.app.smartspacer.plugin.shared.permissions.ExactAlarmCompat
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.PendingIntent_MUTABLE_FLAGS
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

interface FoodScheduler {
    fun hasPermission(): Boolean
    fun scheduleReminder(foodItem: FoodItem)
    fun cancelReminder(foodItemId: Int)
    suspend fun rescheduleAll()
}

class FoodSchedulerImpl(
    private val context: Context,
    private val foodItemDao: FoodItemDao
) : FoodScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun hasPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return alarmManager.canScheduleExactAlarms()
    }

    override fun scheduleReminder(foodItem: FoodItem) {
        cancelReminder(foodItem.id) // 取消该物品的已有闹钟

        if (!foodItem.enabled) {
            return
        }

        val now = System.currentTimeMillis()
        val reminderStartMillis = foodItem.expiryDate - TimeUnit.DAYS.toMillis(foodItem.reminderOffsetDays.toLong())

        // 提醒开始那天的早上 08:00
        val calendar = Calendar.getInstance().apply {
            timeInMillis = reminderStartMillis
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        var alarmTime = calendar.timeInMillis
        // 如果计算出来的早上8点已经过去，但精确提醒开始时间还在未来，就用精确提醒开始时间，保证及时提醒
        if (alarmTime < now && reminderStartMillis > now) {
            alarmTime = reminderStartMillis
        }

        if (alarmTime > now) {
            val intent = FoodAlarmReceiver.createIntent(context, foodItem.id)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                foodItem.id,
                intent,
                PendingIntent_MUTABLE_FLAGS
            )
            ExactAlarmCompat.schedule(
                alarmManager = alarmManager,
                triggerAtMillis = alarmTime,
                pendingIntent = pendingIntent,
                exact = hasPermission()
            )
        }
    }

    override fun cancelReminder(foodItemId: Int) {
        val intent = FoodAlarmReceiver.createIntent(context, foodItemId)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            foodItemId,
            intent,
            PendingIntent_MUTABLE_FLAGS or PendingIntent.FLAG_NO_CREATE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    override suspend fun rescheduleAll() {
        val foodItems = foodItemDao.getAll().first()
        for (foodItem in foodItems) {
            scheduleReminder(foodItem)
        }
    }
}
