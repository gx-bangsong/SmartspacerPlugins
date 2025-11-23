package com.kieronquinn.app.smartspacer.plugin.medication.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.kieronquinn.app.smartspacer.plugin.medication.data.DoseHistory
import com.kieronquinn.app.smartspacer.plugin.medication.data.DoseHistoryDao
import com.kieronquinn.app.smartspacer.plugin.medication.data.MedicationDao
import com.kieronquinn.app.smartspacer.plugin.medication.databinding.ActivityRecordDoseBinding
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.Calendar

class RecordDoseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordDoseBinding
    private val medicationDao by inject<MedicationDao>()
    private val doseHistoryDao by inject<DoseHistoryDao>()
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordDoseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val medicationId = intent.getIntExtra("medication_id", -1)
        if (medicationId == -1) {
            finish()
            return
        }

        lifecycleScope.launch {
            val medication = medicationDao.getById(medicationId) ?: return@launch
            val medicationInfo = "${medication.name} ${medication.dosage ?: ""}"
            binding.textViewMedicationInfo.text = medicationInfo

            binding.buttonTaken.setOnClickListener {
                lifecycleScope.launch {
                    val doseHistory = DoseHistory(
                        medicationId = medication.id,
                        timestamp = System.currentTimeMillis(),
                        status = DoseHistory.Status.TAKEN
                    )
                    doseHistoryDao.insert(doseHistory)
                    val nextDoseTs = calculateNextDose(medication.startDate, gson.fromJson(medication.timesOfDay, Array<String>::class.java).toList())
                    medicationDao.update(medication.copy(nextDoseTs = nextDoseTs))
                    SmartspacerTargetProvider.notifyChange(this@RecordDoseActivity, com.kieronquinn.app.smartspacer.plugin.medication.providers.MedicationProvider::class.java)
                    finish()
                }
            }

            binding.buttonSkip.setOnClickListener {
                lifecycleScope.launch {
                    val doseHistory = DoseHistory(
                        medicationId = medication.id,
                        timestamp = System.currentTimeMillis(),
                        status = DoseHistory.Status.SKIPPED
                    )
                    doseHistoryDao.insert(doseHistory)
                    val nextDoseTs = calculateNextDose(medication.startDate, gson.fromJson(medication.timesOfDay, Array<String>::class.java).toList())
                    medicationDao.update(medication.copy(nextDoseTs = nextDoseTs))
                    SmartspacerTargetProvider.notifyChange(this@RecordDoseActivity, com.kieronquinn.app.smartspacer.plugin.medication.providers.MedicationProvider::class.java)
                    finish()
                }
            }

            binding.buttonSnooze.setOnClickListener {
                lifecycleScope.launch {
                    val snoozeMinutes = getString(com.kieronquinn.app.smartspacer.plugin.medication.R.string.snooze_duration_minutes).toLong()
                    val nextDoseTs = System.currentTimeMillis() + snoozeMinutes * 60 * 1000
                    medicationDao.update(medication.copy(nextDoseTs = nextDoseTs))
                    SmartspacerTargetProvider.notifyChange(this@RecordDoseActivity, com.kieronquinn.app.smartspacer.plugin.medication.providers.MedicationProvider::class.java)
                    finish()
                }
            }
        }
    }

    private fun calculateNextDose(startDate: Long, reminderTimes: List<String>): Long {
        val now = Calendar.getInstance()
        val startCal = Calendar.getInstance().apply { timeInMillis = startDate }

        for (time in reminderTimes.sorted()) {
            val (hour, minute) = time.split(":").map { it.toInt() }
            val doseTime = (startCal.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            }
            if (doseTime.after(now)) {
                return doseTime.timeInMillis
            }
        }

        val (hour, minute) = reminderTimes.sorted().first().split(":").map { it.toInt() }
        return (startCal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }.timeInMillis
    }
}
