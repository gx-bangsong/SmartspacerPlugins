package com.kieronquinn.app.smartspacer.plugin.parcel.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.kieronquinn.app.smartspacer.plugin.parcel.data.ParcelDao
import com.kieronquinn.app.smartspacer.plugin.parcel.data.ParcelItem
import com.kieronquinn.app.smartspacer.plugin.parcel.engine.SmsParserEngine
import com.kieronquinn.app.smartspacer.plugin.parcel.notifications.ParcelNotificationController
import com.kieronquinn.app.smartspacer.plugin.parcel.notifications.ParcelSuppressionRepository
import com.kieronquinn.app.smartspacer.plugin.parcel.providers.ParcelTargetProvider
import com.kieronquinn.app.smartspacer.plugin.parcel.repositories.SettingsRepository
import com.kieronquinn.app.smartspacer.plugin.parcel.repositories.getBlocking
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 实时监听短信广播并解析快递取件信息。
 *
 * Async work runs inside `goAsync()` and `finish()` is guaranteed in `finally`, so the receiver
 * never returns while the coroutine is still running unprotected. A successfully parsed and
 * stored parcel immediately produces one notification; duplicate SMS never re-notify.
 */
class SmsReceiver : BroadcastReceiver(), KoinComponent {
    private val parcelDao by inject<ParcelDao>()
    private val settingsRepository by inject<SettingsRepository>()
    private val notificationController by inject<ParcelNotificationController>()
    private val suppressionRepository by inject<ParcelSuppressionRepository>()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val fullText = messages.joinToString("") { it.displayMessageBody }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                processSms(context, fullText)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun processSms(context: Context, text: String) {
        val engine = SmsParserEngine(context)
        val result = engine.parse(text)

        if (result != null) {
            // 检查是否已存在相同文本或取件码的记录
            val existingRaw = parcelDao.getParcelByRawText(text)
            val duplicate = parcelDao.findDuplicate(result.pickupCode, result.location ?: result.provider)

            if (existingRaw == null && duplicate == null) {
                val parcel = ParcelItem(
                    pickupCode = result.pickupCode,
                    stationName = result.location ?: result.provider,
                    rawText = text,
                    timestamp = System.currentTimeMillis()
                )
                val id = parcelDao.insertParcel(parcel)
                val stored = parcel.copy(id = id)

                // 通知 Smartspacer 刷新显示
                SmartspacerTargetProvider.notifyChange(context, ParcelTargetProvider::class.java)

                // 新取件码：发一次通知；用户 dismiss/unpin 后不再自动发回
                if (!suppressionRepository.isSuppressed(stored.id)) {
                    notificationController.postParcel(stored, promoted = settingsRepository.promotedLiveUpdates.getBlocking())
                }
            }
        }
    }
}
