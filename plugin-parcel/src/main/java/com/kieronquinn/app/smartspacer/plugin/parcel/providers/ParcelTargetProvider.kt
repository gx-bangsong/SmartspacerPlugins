package com.kieronquinn.app.smartspacer.plugin.parcel.providers

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon as AndroidIcon
import com.kieronquinn.app.smartspacer.plugin.parcel.R
import com.kieronquinn.app.smartspacer.plugin.parcel.data.ParcelDao
import com.kieronquinn.app.smartspacer.plugin.parcel.data.ParcelItem
import com.kieronquinn.app.smartspacer.plugin.parcel.ui.fragments.ParcelDetailFragment
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.DialogLauncherActivity
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon as SmartspaceIcon
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ParcelTargetProvider : SmartspacerTargetProvider(), KoinComponent {

    private val parcelDao by inject<ParcelDao>()

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            label = "Parcel Tracker",
            description = "Track parcels from SMS",
            icon = AndroidIcon.createWithResource(context, R.drawable.ic_launcher_foreground),
            configActivity = Intent(context, com.kieronquinn.app.smartspacer.plugin.parcel.ui.activities.SettingsActivity::class.java)
        )
    }

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        val context = this.context ?: return emptyList()
        val now = System.currentTimeMillis()

        // Mark parcels older than 24 hours as expired
        runBlocking { parcelDao.markOldParcelsAsExpired(now - 24 * 60 * 60 * 1000) }

        val pendingParcels = runBlocking { parcelDao.getPendingParcelsList() }

        return pendingParcels.map { parcel ->
            createTarget(context, parcel)
        }
    }

    private fun createTarget(context: Context, parcel: ParcelItem): SmartspaceTarget {
        // More prominent pickup code in the title
        val title = parcel.pickupCode
        val subtitle = parcel.stationName ?: context.getString(R.string.app_name)

        val detailIntent = Intent(context, DialogLauncherActivity::class.java).apply {
            putExtra(DialogLauncherActivity.EXTRA_FRAGMENT_CLASS, ParcelDetailFragment::class.java.name)
            putExtra("parcelId", parcel.id)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return TargetTemplate.Basic(
            id = "parcel_${parcel.id}",
            componentName = ComponentName(context, ParcelTargetProvider::class.java),
            featureType = SmartspaceTarget.FEATURE_REMINDER,
            title = Text(title),
            subtitle = Text(subtitle),
            icon = SmartspaceIcon(AndroidIcon.createWithResource(context, R.drawable.ic_launcher_foreground)),
            onClick = TapAction(intent = detailIntent)
        ).create()
    }

    override fun onDismiss(smartspacerId: String, targetId: String): Boolean {
        return false
    }
}
