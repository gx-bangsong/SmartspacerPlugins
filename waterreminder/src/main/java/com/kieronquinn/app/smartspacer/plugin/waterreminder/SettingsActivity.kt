package com.kieronquinn.app.smartspacer.plugin.waterreminder

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import com.kieronquinn.app.smartspacer.plugin.waterreminder.R
import org.koin.android.ext.android.inject

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : BaseSettingsFragment() {
        override val androidXMl: Int = R.xml.preferences

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            findPreference<Preference>("active_hours")?.setOnPreferenceClickListener {
                showActiveHoursDialog()
                true
            }
        }

        private fun showActiveHoursDialog() {
            val dialogView = layoutInflater.inflate(R.layout.dialog_active_hours, null)
            val startTimePicker = dialogView.findViewById<android.widget.TimePicker>(R.id.start_time_picker)
            val endTimePicker = dialogView.findViewById<android.widget.TimePicker>(R.id.end_time_picker)

            startTimePicker.setIs24HourView(true)
            endTimePicker.setIs24HourView(true)

            val startHour = settings.activeHoursStart / 60
            val startMinute = settings.activeHoursStart % 60
            startTimePicker.hour = startHour
            startTimePicker.minute = startMinute

            val endHour = settings.activeHoursEnd / 60
            val endMinute = settings.activeHoursEnd % 60
            endTimePicker.hour = endHour
            endTimePicker.minute = endMinute

            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Set Active Hours")
                .setView(dialogView)
                .setPositiveButton("Set") { _, _ ->
                    val startTotalMinutes = startTimePicker.hour * 60 + startTimePicker.minute
                    val endTotalMinutes = endTimePicker.hour * 60 + endTimePicker.minute
                    settings.activeHoursStart = startTotalMinutes
                    settings.activeHoursEnd = endTotalMinutes
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}