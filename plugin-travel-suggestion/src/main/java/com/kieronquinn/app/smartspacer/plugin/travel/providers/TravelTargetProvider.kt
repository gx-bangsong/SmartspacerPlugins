package com.kieronquinn.app.smartspacer.plugin.travel.providers

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon as AndroidIcon
import com.kieronquinn.app.smartspacer.plugin.travel.R
import com.kieronquinn.app.smartspacer.plugin.travel.data.TravelInfoDao
import com.kieronquinn.app.smartspacer.plugin.travel.data.TravelInfoItem
import com.kieronquinn.app.smartspacer.plugin.travel.ui.activities.TravelActionActivity
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon as SmartspaceIcon
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TravelTargetProvider : SmartspacerTargetProvider(), KoinComponent {

    private val travelInfoDao by inject<TravelInfoDao>()

    override fun getConfig(smartspacerId: String?): Config {
        val safeContext = context!!
        return Config(
            label = safeContext.getString(R.string.app_name),
            description = safeContext.getString(R.string.settings_enable_sms_summary),
            icon = AndroidIcon.createWithResource(safeContext, R.mipmap.ic_launcher),
            configActivity = Intent(safeContext, com.kieronquinn.app.smartspacer.plugin.travel.ui.activities.SettingsActivity::class.java)
        )
    }

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        val context = this.context ?: return emptyList()

        val now = System.currentTimeMillis()
        val trips = runBlocking { travelInfoDao.getUnusedTrips(now) }

        return trips.map { trip ->
            createTarget(context, trip)
        }
    }

    private fun createTarget(context: Context, trip: TravelInfoItem): SmartspaceTarget {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeStr = sdf.format(Date(trip.departureTime))

        val titleText = if (!trip.arrivalStation.isNullOrEmpty()) {
            context.getString(
                R.string.target_format_with_to,
                trip.trainNumber,
                trip.departureStation,
                trip.arrivalStation,
                timeStr,
                trip.seat ?: ""
            )
        } else {
            context.getString(
                R.string.target_format_no_to,
                trip.trainNumber,
                trip.departureStation,
                timeStr,
                trip.seat ?: ""
            )
        }

        val subtitleText = trip.passengerName ?: context.getString(R.string.app_name)

        val actionIntent = Intent(context, TravelActionActivity::class.java).apply {
            putExtra("trainNumber", trip.trainNumber)
            putExtra("departureStation", trip.departureStation)
            putExtra("arrivalStation", trip.arrivalStation ?: "")
            putExtra("seat", trip.seat ?: "")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return TargetTemplate.Basic(
            id = "travel_${trip.id}",
            componentName = ComponentName(context, TravelTargetProvider::class.java),
            featureType = SmartspaceTarget.FEATURE_REMINDER,
            title = Text(titleText),
            subtitle = Text(subtitleText),
            icon = SmartspaceIcon(AndroidIcon.createWithResource(context, R.mipmap.ic_launcher), shouldTint = false),
            onClick = TapAction(intent = actionIntent)
        ).create()
    }

    override fun onDismiss(smartspacerId: String, targetId: String): Boolean {
        val itemId = targetId.removePrefix("travel_").toIntOrNull()
        if (itemId != null) {
            runBlocking {
                val item = travelInfoDao.getById(itemId)
                if (item != null) {
                    travelInfoDao.update(item.copy(isUsed = true))
                }
            }
            return true
        }
        return false
    }
}
