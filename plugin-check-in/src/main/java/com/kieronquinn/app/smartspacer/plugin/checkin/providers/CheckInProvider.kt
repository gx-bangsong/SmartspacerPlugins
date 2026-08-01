package com.kieronquinn.app.smartspacer.plugin.checkin.providers

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon as AndroidIcon
import com.kieronquinn.app.smartspacer.plugin.checkin.R
import com.kieronquinn.app.smartspacer.plugin.checkin.data.CheckInDao
import com.kieronquinn.app.smartspacer.plugin.checkin.data.CheckInItem
import com.kieronquinn.app.smartspacer.plugin.checkin.ui.activities.CheckInActionActivity
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

class CheckInProvider : SmartspacerTargetProvider(), KoinComponent {

    private val checkInDao by inject<CheckInDao>()

    override fun getConfig(smartspacerId: String?): Config {
        val safeContext = context!!
        return Config(
            label = safeContext.getString(R.string.app_name),
            description = safeContext.getString(R.string.settings_title),
            icon = AndroidIcon.createWithResource(safeContext, R.mipmap.ic_launcher),
            configActivity = Intent(safeContext, com.kieronquinn.app.smartspacer.plugin.checkin.ui.activities.SettingsActivity::class.java)
        )
    }

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        val context = this.context ?: return emptyList()

        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val record = runBlocking { checkInDao.getByDate(todayDate) }

        return listOf(createTarget(context, record))
    }

    private fun createTarget(context: Context, record: CheckInItem?): SmartspaceTarget {
        val titleText: String
        val timeSdf = SimpleDateFormat("HH:mm", Locale.getDefault())

        if (record == null) {
            titleText = context.getString(R.string.target_unpunched)
        } else if (record.checkOutTime == null) {
            val checkInStr = timeSdf.format(Date(record.checkInTime ?: System.currentTimeMillis()))
            titleText = context.getString(R.string.target_punched_in, checkInStr)
        } else {
            val checkOutStr = timeSdf.format(Date(record.checkOutTime))
            titleText = context.getString(R.string.target_punched_out, checkOutStr)
        }

        val actionIntent = Intent(context, CheckInActionActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return TargetTemplate.Basic(
            id = "check_in_widget",
            componentName = ComponentName(context, CheckInProvider::class.java),
            featureType = SmartspaceTarget.FEATURE_REMINDER,
            title = Text(titleText),
            subtitle = Text(context.getString(R.string.app_name)),
            icon = SmartspaceIcon(AndroidIcon.createWithResource(context, R.mipmap.ic_launcher), shouldTint = false),
            onClick = TapAction(intent = actionIntent)
        ).create()
    }

    override fun onDismiss(smartspacerId: String, targetId: String): Boolean {
        return false
    }
}
