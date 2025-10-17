package com.kieronquinn.app.smartspacer.plugin.medicationreminder.data

import kotlinx.coroutines.flow.Flow

class MedicationRepository(
    private val medicationDao: MedicationDao,
    private val takenDoseDao: TakenDoseDao
) {
    val allMedications: Flow<List<Medication>> = medicationDao.getAll()

    fun getTakenDosesForMedication(medicationId: Int): Flow<List<TakenDose>> {
        return takenDoseDao.getTakenDosesForMedication(medicationId)
    }

    suspend fun insert(medication: Medication) {
        medicationDao.insert(medication)
    }

    suspend fun insert(takenDose: TakenDose) {
        takenDoseDao.insert(takenDose)
    }

    suspend fun update(medication: Medication) {
        medicationDao.update(medication)
    }

    suspend fun deleteById(id: Int) {
        medicationDao.deleteById(id)
    }
}