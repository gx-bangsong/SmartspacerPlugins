package com.kieronquinn.app.smartspacer.plugin.travel.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.kieronquinn.app.smartspacer.plugin.travel.data.TravelInfoDao
import com.kieronquinn.app.smartspacer.plugin.travel.data.TravelInfoItem
import com.kieronquinn.app.smartspacer.plugin.travel.repositories.TravelSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.travel.repositories.TravelScheduler
import com.kieronquinn.app.smartspacer.plugin.travel.providers.TravelTargetProvider
import com.kieronquinn.app.smartspacer.shared.smsparser.SmsParser
import com.kieronquinn.app.smartspacer.shared.smsparser.ParseResultStatus
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TravelSmsReceiver : BroadcastReceiver(), KoinComponent {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val travelInfoDao by inject<TravelInfoDao>()
    private val settingsRepository by inject<TravelSettingsRepository>()
    private val travelScheduler by inject<TravelScheduler>()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val pendingResult = goAsync()
            scope.launch {
                try {
                    val isEnabled = settingsRepository.isSmsParsingEnabled.first()
                    if (!isEnabled) return@launch

                    val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                    val fullText = messages.joinToString("") { it.displayMessageBody }

                    val parser = SmsParser(context)
                    val result = parser.parseTravelInfo(fullText)

                    if (result.status == ParseResultStatus.SUCCESS && result.travelInfo != null) {
                        val parsed = result.travelInfo!!

                        val existing = travelInfoDao.getUnusedTrips(System.currentTimeMillis())
                        val isDuplicate = existing.any {
                            it.trainNumber == parsed.trainNumber &&
                            Math.abs(it.departureTime - parsed.departureTime) < 5 * 60 * 1000
                        }

                        if (!isDuplicate) {
                            val travelItem = TravelInfoItem(
                                trainNumber = parsed.trainNumber,
                                departureStation = parsed.departureStation,
                                arrivalStation = parsed.arrivalStation,
                                departureTime = parsed.departureTime,
                                seat = parsed.seat,
                                passengerName = parsed.passengerName,
                                source = "sms"
                            )
                            travelInfoDao.insert(travelItem)

                            travelScheduler.rescheduleAll()

                            SmartspacerTargetProvider.notifyChange(context, TravelTargetProvider::class.java)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
