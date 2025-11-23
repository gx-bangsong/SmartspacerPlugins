package com.kieronquinn.app.smartspacer.plugin.medication.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.kieronquinn.app.smartspacer.plugin.medication.R
import com.kieronquinn.app.smartspacer.plugin.medication.data.DoseHistory
import com.kieronquinn.app.smartspacer.plugin.medication.data.DoseHistoryDao
import com.kieronquinn.app.smartspacer.plugin.medication.data.MedicationDao
import com.kieronquinn.app.smartspacer.plugin.medication.databinding.FragmentRecordDoseBinding
import com.kieronquinn.app.smartspacer.plugin.medication.providers.MedicationProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.Calendar

class RecordDoseFragment : DialogFragment() {

    private lateinit var binding: FragmentRecordDoseBinding
    private val medicationDao by inject<MedicationDao>()
    private val doseHistoryDao by inject<DoseHistoryDao>()
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecordDoseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val medicationId = requireArguments().getInt("medication_id", -1)
        if (medicationId == -1) {
            dismiss()
            return
        }

        lifecycleScope.launch {
            val medication = medicationDao.getById(medicationId) ?: return@launch
            val medicationInfo = "${medication.name} ${medication.dosage ?: ""}"
            binding.textViewMedicationInfo.text = medicationInfo

            binding.buttonTaken.setOnClickListener {
                handleDoseAction(medication, DoseHistory.Status.TAKEN)
            }

            binding.buttonSkip.setOnClickListener {
                handleDoseAction(medication, DoseHistory.Status.SKIPPED)
            }

            binding.buttonSnooze.setOnClickListener {
                lifecycleScope.launch {
                    val snoozeMinutes = getString(R.string.snooze_duration_minutes).toLong()
                    val nextDoseTs = System.currentTimeMillis() + snoozeMinutes * 60 * 1000
                    medicationDao.update(medication.copy(nextDoseTs = nextDoseTs))
                    SmartspacerTargetProvider.notifyChange(requireContext(), MedicationProvider::class.java)
                    dismiss()
                }
            }
        }
    }

    private fun handleDoseAction(medication: com.kieronquinn.app.smartspacer.plugin.medication.data.Medication, status: DoseHistory.Status) {
        lifecycleScope.launch {
            val doseHistory = DoseHistory(
                medicationId = medication.id,
                timestamp = System.currentTimeMillis(),
                status = status
            )
            doseHistoryDao.insert(doseHistory)
            val nextDoseTs = calculateNextDose(medication.startDate, gson.fromJson(medication.timesOfDay, Array<String>::class.java).toList())
            medicationDao.update(medication.copy(nextDoseTs = nextDoseTs))
            SmartspacerTargetProvider.notifyChange(requireContext(), MedicationProvider::class.java)
            dismiss()
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
