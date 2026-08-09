package com.kieronquinn.app.smartspacer.plugin.travel.ui.activities

import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kieronquinn.app.smartspacer.plugin.travel.repositories.TravelSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class TravelActionActivity : AppCompatActivity() {

    private val settingsRepository by inject<TravelSettingsRepository>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val trainNumber = intent.getStringExtra("trainNumber") ?: ""
        val departureStation = intent.getStringExtra("departureStation") ?: ""
        val arrivalStation = intent.getStringExtra("arrivalStation") ?: ""
        val seat = intent.getStringExtra("seat") ?: ""

        if (trainNumber.isNotEmpty()) {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipText = if (arrivalStation.isNotEmpty()) {
                "$trainNumber $departureStation → $arrivalStation $seat"
            } else {
                "$trainNumber $departureStation $seat"
            }
            clipboard.setPrimaryClip(ClipData.newPlainText("Travel Info", clipText))
            Toast.makeText(this, "出行信息已复制到剪贴板", Toast.LENGTH_SHORT).show()
        }

        CoroutineScope(Dispatchers.Main).launch {
            val target = settingsRepository.jumpTarget.first()
            try {
                when (target) {
                    "12306" -> {
                        val launchIntent = packageManager.getLaunchIntentForPackage("com.chinatms")
                        if (launchIntent != null) {
                            startActivity(launchIntent)
                        } else {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.12306.cn")))
                        }
                    }
                    "umetrip" -> {
                        val launchIntent = packageManager.getLaunchIntentForPackage("com.umetrip.android.msky.app")
                        if (launchIntent != null) {
                            startActivity(launchIntent)
                        } else {
                            Toast.makeText(this@TravelActionActivity, "未安装航旅纵横", Toast.LENGTH_SHORT).show()
                        }
                    }
                    "maps" -> {
                        if (departureStation.isNotEmpty()) {
                            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$departureStation"))
                            startActivity(mapIntent)
                        }
                    }
                    else -> {
                        // "none"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            finish()
        }
    }
}
