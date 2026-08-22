package com.kieronquinn.app.smartspacer.plugin.travel.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.kieronquinn.app.smartspacer.plugin.travel.data.TravelInfoDao
import com.kieronquinn.app.smartspacer.plugin.travel.data.TravelInfoItem
import com.kieronquinn.app.smartspacer.plugin.travel.data.TravelTripSave
import com.kieronquinn.app.smartspacer.plugin.travel.logic.TravelDedupe
import com.kieronquinn.app.smartspacer.plugin.travel.logic.TripKey
import com.kieronquinn.app.smartspacer.plugin.travel.notifications.TravelNotificationController
import com.kieronquinn.app.smartspacer.plugin.travel.notifications.isWithinDepartureWindow
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

/**
 * Parses incoming travel SMS and stores new trips. Backend SMS arrival is not a user-initiated
 * parse operation, so no "parsing" Live Update is posted here — instead the result is shown as a
 * regular high-visibility notification, which is upgraded in place to a promoted Live Update once
 * the trip enters the departure window (T-30). The same trip can never be inserted twice.
 */
class TravelSmsReceiver : BroadcastReceiver(), KoinComponent {
    private val travelInfoDao by inject<TravelInfoDao>()
    private val settingsRepository by inject<TravelSettingsRepository>()
    private val travelScheduler by inject<TravelScheduler>()
    private val notificationController by inject<TravelNotificationController>()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val isEnabled = settingsRepository.isSmsParsingEnabled.first()
                if (!isEnabled) return@launch

                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                val fullText = messages.joinToString("") { it.displayMessageBody }

                val customRulesJson = settingsRepository.customRulesJson.first()
                val parser = customRulesJson?.let { SmsParser(it) } ?: SmsParser(context)
                val result = parser.parseTravelInfo(fullText)

                if (result.status == ParseResultStatus.SUCCESS && result.travelInfo != null) {
                    val parsed = result.travelInfo!!

                    val existing = travelInfoDao.getUnusedTrips(System.currentTimeMillis())
                    val isDuplicate = TravelDedupe.isDuplicate(
                        existing.map { TripKey(it.trainNumber, it.departureTime) },
                        parsed.trainNumber,
                        parsed.departureTime
                    )

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
                        val savedItem = TravelTripSave.afterInsert(
                            travelItem,
                            travelInfoDao.insert(travelItem)
                        )

                        travelScheduler.scheduleReminder(savedItem)

                        SmartspacerTargetProvider.notifyChange(context, TravelTargetProvider::class.java)

                        val now = System.currentTimeMillis()
                        if (savedItem.isWithinDepartureWindow(now)) {
                            // Already inside the departure window: go straight to the Live Update.
                            notificationController.postTripLiveUpdate(savedItem)
                        } else {
                            // Departure is still far away: normal result notification; the T-30
                            // alarm upgrades the same notification ID to a Live Update later.
                            notificationController.postTripResult(savedItem)
                        }
                    }
                }
            } catch (e: Exception) {
                // Parsing must never crash the receiver; the SMS is left untouched.
            } finally {
                pendingResult.finish()
            }
        }
    }
}
