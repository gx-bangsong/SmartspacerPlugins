package com.kieronquinn.app.smartspacer.plugin.medicationreminder.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.R

class NotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val medicationId = inputData.getInt("medication_id", -1)
        val medicationName = inputData.getString("medication_name")
        val medicationDosage = inputData.getString("medication_dosage")

        if (medicationId != -1 && medicationName != null) {
            sendNotification(medicationId, medicationName, medicationDosage)
        }

        return Result.success()
    }

    private fun sendNotification(medicationId: Int, medicationName: String, medicationDosage: String?) {
        val channelId = "medication_reminder_channel"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Medication Reminders",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = androidx.core.app.NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Time to take your medication")
            .setContentText("Take $medicationName ${medicationDosage ?: ""}")
            .setSmallIcon(R.drawable.ic_notification)
            .build()

        notificationManager.notify(medicationId, notification)
    }
}