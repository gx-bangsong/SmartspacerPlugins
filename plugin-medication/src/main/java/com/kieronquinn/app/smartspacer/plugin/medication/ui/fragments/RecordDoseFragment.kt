package com.kieronquinn.app.smartspacer.plugin.medication.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.smartspacer.plugin.medication.R
import com.kieronquinn.app.smartspacer.plugin.medication.data.Medication
import com.kieronquinn.app.smartspacer.plugin.medication.data.MedicationDao
import com.kieronquinn.app.smartspacer.plugin.medication.data.DoseHistory
import com.kieronquinn.app.smartspacer.plugin.medication.data.DoseHistoryDao
import com.kieronquinn.app.smartspacer.plugin.medication.data.ScheduleType
import com.kieronquinn.app.smartspacer.plugin.medication.databinding.FragmentRecordDoseBinding
import com.kieronquinn.app.smartspacer.plugin.medication.providers.MedicationProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class RecordDoseFragment : DialogFragment() {

    private lateinit var binding: FragmentRecordDoseBinding
    private val medicationDao by inject<MedicationDao>()
    private val doseHistoryDao by inject<DoseHistoryDao>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecordDoseBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val medicationId = requireArguments().getInt("medicationId", -1)
        if (medicationId == -1) {
            dismiss()
            return
        }
        lifecycleScope.launch {
            val medication = medicationDao.getById(medicationId)
            if (medication == null) {
                dismiss()
                return@launch
            }
            binding.textViewDoseInfo.text = "Take ${medication.name}"

            binding.buttonTaken.setOnClickListener {
                handleDose(medication, DoseHistory.Status.TAKEN)
            }

            binding.buttonSkip.setOnClickListener {
                handleDose(medication, DoseHistory.Status.SKIPPED)
            }
        }
    }

    private fun handleDose(medication: Medication, status: DoseHistory.Status) {
        lifecycleScope.launch {
            val dose = DoseHistory(
                timestamp = System.currentTimeMillis(),
                medicationId = medication.id,
                status = status
            )
            doseHistoryDao.insert(dose)

            val intervalMillis = when (medication.scheduleType) {
                ScheduleType.EVERY_X_HOURS -> (medication.intervalHours ?: 0) * 60 * 60 * 1000L
                ScheduleType.EVERY_X_DAYS -> (medication.intervalDays ?: 0) * 24 * 60 * 60 * 1000L
                else -> 0L // Not applicable for other types
            }
            val nextDoseTs = medication.nextDoseTs + intervalMillis
            val updatedMedication = medication.copy(nextDoseTs = nextDoseTs)
            medicationDao.update(updatedMedication)

            SmartspacerTargetProvider.notifyChange(requireContext(), MedicationProvider::class.java)
            if (status == DoseHistory.Status.TAKEN) {
                Toast.makeText(requireContext(), R.string.medication_record_success, Toast.LENGTH_SHORT).show()
            }
            dismiss()
        }
    }
}
