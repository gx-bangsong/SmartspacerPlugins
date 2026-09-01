package com.kieronquinn.app.smartspacer.plugin.medication.repositories

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import com.kieronquinn.app.smartspacer.plugin.medication.data.Medication
import com.kieronquinn.app.smartspacer.plugin.medication.data.MedicationDao
import com.kieronquinn.app.smartspacer.plugin.medication.receivers.MedicationAlarmReceiver
import com.kieronquinn.app.smartspacer.plugin.shared.permissions.ExactAlarmCompat
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.PendingIntent_MUTABLE_FLAGS
import kotlinx.coroutines.flow.first

interface MedicationScheduler {
    fun hasPermission(): Boolean
    fun scheduleAlarm(medication: Medication)
    fun cancelAlarm(medicationId: Int)
    suspend fun rescheduleAll()
}

class MedicationSchedulerImpl(
    private val context: Context,
    private val medicationDao: MedicationDao
) : MedicationScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun hasPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return alarmManager.canScheduleExactAlarms()
    }

    override fun scheduleAlarm(medication: Medication) {
        cancelAlarm(medication.id) // Cancel existing alarms first

        if (!medication.enabled) {
            return
        }

        val now = System.currentTimeMillis()
        val nextDoseAlarmTime = medication.nextDoseTs
        val warningAlarmTime = medication.nextDoseTs - 60 * 60 * 1000L

        // 调度精确服药时间闹钟
        if (nextDoseAlarmTime > now) {
            val intent = MedicationAlarmReceiver.createIntent(context, medication.id)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                medication.id * 2,
                intent,
                PendingIntent_MUTABLE_FLAGS
            )
            ExactAlarmCompat.schedule(
                alarmManager = alarmManager,
                triggerAtMillis = nextDoseAlarmTime,
                pendingIntent = pendingIntent,
                exact = hasPermission()
            )
        }

        // 调度提前60分钟的展示窗口闹钟
        if (warningAlarmTime > now) {
            val intent = MedicationAlarmReceiver.createIntent(context, medication.id)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                medication.id * 2 + 1,
                intent,
                PendingIntent_MUTABLE_FLAGS
            )
            ExactAlarmCompat.schedule(
                alarmManager = alarmManager,
                triggerAtMillis = warningAlarmTime,
                pendingIntent = pendingIntent,
                exact = hasPermission()
            )
        }
    }

    override fun cancelAlarm(medicationId: Int) {
        val intent = MedicationAlarmReceiver.createIntent(context, medicationId)

        val pendingIntent1 = PendingIntent.getBroadcast(
            context,
            medicationId * 2,
            intent,
            PendingIntent_MUTABLE_FLAGS or PendingIntent.FLAG_NO_CREATE
        )
        if (pendingIntent1 != null) {
            alarmManager.cancel(pendingIntent1)
            pendingIntent1.cancel()
        }

        val pendingIntent2 = PendingIntent.getBroadcast(
            context,
            medicationId * 2 + 1,
            intent,
            PendingIntent_MUTABLE_FLAGS or PendingIntent.FLAG_NO_CREATE
        )
        if (pendingIntent2 != null) {
            alarmManager.cancel(pendingIntent2)
            pendingIntent2.cancel()
        }
    }

    override suspend fun rescheduleAll() {
        val medications = medicationDao.getAll().first()
        for (medication in medications) {
            scheduleAlarm(medication)
        }
    }
}
