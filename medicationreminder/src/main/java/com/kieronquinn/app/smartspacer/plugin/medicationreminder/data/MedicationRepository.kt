package com.kieronquinn.app.smartspacer.plugin.medicationreminder.data

import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.model.DoseLog
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.data.model.Medication
import kotlinx.coroutines.flow.Flow

class MedicationRepository(private val medicationDao: MedicationDao) {

    fun getMedications(): Flow<List<Medication>> {
        return medicationDao.getMedications()
    }

    suspend fun addMedication(medication: Medication) {
        medicationDao.insertMedication(medication)
    }

    suspend fun deleteMedication(medicationId: Int) {
        medicationDao.deleteMedication(medicationId)
    }

    suspend fun logDose(medicationId: Int, timestamp: Long) {
        val doseLog = DoseLog(medicationId = medicationId, doseTimestamp = timestamp)
        medicationDao.logDose(doseLog)
    }

    fun getDoseLogs(medicationId: Int): Flow<List<DoseLog>> {
        return medicationDao.getDoseLogsForMedication(medicationId)
    }
}