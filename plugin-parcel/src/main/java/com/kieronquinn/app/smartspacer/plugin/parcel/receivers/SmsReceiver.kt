package com.kieronquinn.app.smartspacer.plugin.parcel.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.kieronquinn.app.smartspacer.plugin.parcel.data.ParcelDao
import com.kieronquinn.app.smartspacer.plugin.parcel.data.ParcelItem
import com.kieronquinn.app.smartspacer.plugin.parcel.engine.SmsParserEngine
import com.kieronquinn.app.smartspacer.plugin.parcel.providers.ParcelTargetProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 实时监听短信广播并解析快递取件信息
 */
class SmsReceiver : BroadcastReceiver(), KoinComponent {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val parcelDao by inject<ParcelDao>()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            val fullText = messages.joinToString("") { it.displayMessageBody }

            processSms(context, fullText)
        }
    }

    private fun processSms(context: Context, text: String) {
        scope.launch {
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
                    parcelDao.insertParcel(parcel)
                    // 通知 Smartspacer 刷新显示
                    SmartspacerTargetProvider.notifyChange(context, ParcelTargetProvider::class.java)
                }
            }
        }
    }
}
