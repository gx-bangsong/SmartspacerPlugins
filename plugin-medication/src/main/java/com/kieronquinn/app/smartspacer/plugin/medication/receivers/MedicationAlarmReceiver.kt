package com.kieronquinn.app.smartspacer.plugin.medication.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.medication.data.MedicationDao
import com.kieronquinn.app.smartspacer.plugin.medication.providers.MedicationProvider
import com.kieronquinn.app.smartspacer.plugin.medication.repositories.MedicationScheduler
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.verifySecurity
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.applySecurity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MedicationAlarmReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        private const val EXTRA_MEDICATION_ID = "medication_id"

        fun createIntent(context: Context, medicationId: Int): Intent {
            return Intent(context, MedicationAlarmReceiver::class.java).apply {
                putExtra(EXTRA_MEDICATION_ID, medicationId)
                applySecurity(context)
            }
        }
    }

    private val medicationDao by inject<MedicationDao>()
    private val medicationScheduler by inject<MedicationScheduler>()

    override fun onReceive(context: Context, intent: Intent) {
        intent.verifySecurity(context)
        val medicationId = intent.getIntExtra(EXTRA_MEDICATION_ID, -1)
        if (medicationId == -1) return

        SmartspacerTargetProvider.notifyChange(context, MedicationProvider::class.java)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val medication = medicationDao.getById(medicationId)
                if (medication != null) {
                    medicationScheduler.scheduleAlarm(medication)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
