package com.kieronquinn.app.smartspacer.plugin.parcel.engine

import android.content.Context
import android.provider.Telephony
import com.kieronquinn.app.smartspacer.plugin.parcel.data.ParcelDao
import com.kieronquinn.app.smartspacer.plugin.parcel.data.ParcelItem
import com.kieronquinn.app.smartspacer.plugin.parcel.providers.ParcelTargetProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class InboxScanner(private val context: Context) : KoinComponent {

    private val parcelDao by inject<ParcelDao>()

    suspend fun scan() = withContext(Dispatchers.IO) {
        val cursor = context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms.BODY, Telephony.Sms.DATE),
            null,
            null,
            Telephony.Sms.DATE + " DESC LIMIT 100"
        )

        cursor?.use {
            val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
            val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)
            val engine = SmsParserEngine(context)

            while (it.moveToNext()) {
                val text = it.getString(bodyIndex)
                val date = it.getLong(dateIndex)

                // Only scan messages from the last 3 days
                if (System.currentTimeMillis() - date > 3 * 24 * 60 * 60 * 1000) continue

                val result = engine.parse(text)
                if (result != null) {
                    val existingRaw = parcelDao.getParcelByRawText(text)
                    val duplicate = parcelDao.findDuplicate(result.pickupCode, result.location ?: result.provider)

                    if (existingRaw == null && duplicate == null) {
                        val parcel = ParcelItem(
                            pickupCode = result.pickupCode,
                            stationName = result.location ?: result.provider,
                            rawText = text,
                            timestamp = date
                        )
                        parcelDao.insertParcel(parcel)
                    }
                }
            }
            SmartspacerTargetProvider.notifyChange(context, ParcelTargetProvider::class.java)
        }
    }
}
