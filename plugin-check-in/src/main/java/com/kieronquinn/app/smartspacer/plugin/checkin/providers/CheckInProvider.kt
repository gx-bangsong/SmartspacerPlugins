package com.kieronquinn.app.smartspacer.plugin.checkin.providers

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon as AndroidIcon
import com.kieronquinn.app.smartspacer.plugin.checkin.R
import com.kieronquinn.app.smartspacer.plugin.checkin.data.CheckInDao
import com.kieronquinn.app.smartspacer.plugin.checkin.data.CheckInItem
import com.kieronquinn.app.smartspacer.plugin.checkin.ui.activities.CheckInActionActivity
import com.kieronquinn.app.smartspacer.plugin.checkin.repositories.CheckInSettingsRepository
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon as SmartspaceIcon
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CheckInProvider : SmartspacerTargetProvider(), KoinComponent {

    private val checkInDao by inject<CheckInDao>()
    private val settingsRepository by inject<CheckInSettingsRepository>()

    override fun getConfig(smartspacerId: String?): Config {
        val safeContext = context!!
        return Config(
            label = safeContext.getString(R.string.app_name),
            description = safeContext.getString(R.string.settings_title),
            icon = AndroidIcon.createWithResource(safeContext, R.drawable.ic_launcher_greyscale),
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
        val timeSdf = SimpleDateFormat("HH:mm", Locale.getDefault())

        val checkInOnly = runBlocking { settingsRepository.checkInOnly.first() }
        val startTime = runBlocking { settingsRepository.workStartTime.first() }
        val endTime = runBlocking { settingsRepository.workEndTime.first() }
        val startReached = timeReached(startTime)
        val endReached = timeReached(endTime)

        val titleText: String = when {
            // 没有记录，或只有手动补录的下班时间而没有上班时间
            record == null || record.checkInTime == null -> {
                if (startReached) {
                    // 已到上班时间仍未打卡
                    context.getString(R.string.target_remind_check_in)
                } else {
                    context.getString(R.string.target_unpunched)
                }
            }
            record.checkOutTime == null -> {
                // 已打上班卡，未打下班卡
                if (!checkInOnly && endReached) {
                    // 已到下班时间仍未打卡
                    context.getString(R.string.target_remind_check_out)
                } else {
                    val checkInStr = timeSdf.format(Date(record.checkInTime))
                    context.getString(R.string.target_punched_in, checkInStr)
                }
            }
            else -> {
                if (checkInOnly) {
                    // 仅上班打卡模式下永远显示上班状态
                    val checkInStr = timeSdf.format(Date(record.checkInTime))
                    context.getString(R.string.target_punched_in, checkInStr)
                } else {
                    val checkOutStr = timeSdf.format(Date(record.checkOutTime))
                    context.getString(R.string.target_punched_out, checkOutStr)
                }
            }
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

    /**
     * Whether the current time of day has already passed the given "HH:mm".
     */
    private fun timeReached(timeStr: String): Boolean {
        val parts = timeStr.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: return false
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: return false
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return now.timeInMillis >= target.timeInMillis
    }
}
