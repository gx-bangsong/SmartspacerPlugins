package com.kieronquinn.app.smartspacer.plugin.parcel.providers

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon as AndroidIcon
import com.kieronquinn.app.smartspacer.plugin.parcel.R
import com.kieronquinn.app.smartspacer.plugin.parcel.data.ParcelDao
import com.kieronquinn.app.smartspacer.plugin.parcel.data.ParcelItem
import com.kieronquinn.app.smartspacer.plugin.parcel.notifications.ParcelLiveUpdatePublisher
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
    private val liveUpdatePublisher by inject<ParcelLiveUpdatePublisher>()

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            label = "Parcel Tracker",
            description = "Track parcels from SMS",
            icon = AndroidIcon.createWithResource(context, R.mipmap.ic_launcher),
            configActivity = Intent(context, com.kieronquinn.app.smartspacer.plugin.parcel.ui.activities.SettingsActivity::class.java)
        )
    }

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        val context = this.context ?: return emptyList()

        // 过期逻辑已移至 ParcelWorker，这里只负责显示
        val pendingParcels = runBlocking {
            liveUpdatePublisher.publishPending()
            parcelDao.getPendingParcelsList()
        }

        return pendingParcels.map { parcel ->
            createTarget(context, parcel)
        }
    }

    private fun createTarget(context: Context, parcel: ParcelItem): SmartspaceTarget {
        // 在标题中突出取件码
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
            icon = SmartspaceIcon(AndroidIcon.createWithResource(context, R.mipmap.ic_launcher), shouldTint = false),
            onClick = TapAction(intent = detailIntent)
        ).create()
    }

    override fun onDismiss(smartspacerId: String, targetId: String): Boolean {
        return false
    }
}
