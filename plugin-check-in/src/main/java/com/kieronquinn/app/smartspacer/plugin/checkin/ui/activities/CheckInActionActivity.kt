package com.kieronquinn.app.smartspacer.plugin.checkin.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.kieronquinn.app.smartspacer.plugin.checkin.data.CheckInDao
import com.kieronquinn.app.smartspacer.plugin.checkin.data.CheckInItem
import com.kieronquinn.app.smartspacer.plugin.checkin.repositories.CheckInSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.checkin.providers.CheckInProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// This activity uses a platform translucent theme and has no AppCompat UI.
// FragmentActivity avoids AppCompat's Theme.AppCompat requirement.
class CheckInActionActivity : FragmentActivity() {

    private val checkInDao by inject<CheckInDao>()
    private val settingsRepository by inject<CheckInSettingsRepository>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val now = System.currentTimeMillis()
                val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now))
                val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(now))

                val record = checkInDao.getByDate(todayDate)
                if (record == null) {
                    val newRecord = CheckInItem(
                        date = todayDate,
                        checkInTime = now,
                        checkOutTime = null
                    )
                    checkInDao.insert(newRecord)
                    Toast.makeText(this@CheckInActionActivity, "上班打卡成功: $timeStr", Toast.LENGTH_SHORT).show()
                } else if (record.checkOutTime == null) {
                    val updated = record.copy(checkOutTime = now)
                    checkInDao.update(updated)
                    Toast.makeText(this@CheckInActionActivity, "下班打卡成功: $timeStr", Toast.LENGTH_SHORT).show()
                } else {
                    val updated = record.copy(checkOutTime = now)
                    checkInDao.update(updated)
                    Toast.makeText(this@CheckInActionActivity, "已更新下班打卡: $timeStr", Toast.LENGTH_SHORT).show()
                }

                // Sync UI refresh
                SmartspacerTargetProvider.notifyChange(this@CheckInActionActivity, CheckInProvider::class.java)

                // App Linkage launch
                val targetApp = settingsRepository.linkApp.first()
                val packageName = when (targetApp) {
                    "wecom" -> "com.tencent.wework"
                    "dingtalk" -> "com.alibaba.android.rimet"
                    "feishu" -> "com.ss.android.lark"
                    "feilian" -> "com.bytedance.feilian"
                    else -> null
                }

                if (packageName != null) {
                    val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                    if (launchIntent != null) {
                        startActivity(launchIntent)
                    } else {
                        Toast.makeText(this@CheckInActionActivity, "未安装对应的考勤 App", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            finish()
        }
    }
}
