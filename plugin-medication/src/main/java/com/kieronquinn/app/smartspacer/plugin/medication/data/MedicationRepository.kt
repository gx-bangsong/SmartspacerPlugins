package com.kieronquinn.app.smartspacer.plugin.medication.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface MedicationRepository {
    val medications: Flow<List<Medication>>
    suspend fun addMedication(medication: Medication)
    suspend fun updateMedication(medication: Medication)
    suspend fun getMedication(id: Int): Medication?
}

class MedicationRepositoryImpl(context: Context, private val gson: Gson) : MedicationRepository {

    companion object {
        private const val PREFERENCES_NAME = "medication_prefs"
        private const val MEDICATIONS_KEY = "medications"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private val _medications = MutableStateFlow<List<Medication>>(emptyList())
    override val medications: Flow<List<Medication>> = _medications.asStateFlow()

    init {
        _medications.value = loadMedications()
    }

    private fun loadMedications(): List<Medication> {
        val json = sharedPreferences.getString(MEDICATIONS_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<List<Medication>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    private suspend fun saveMedications(medications: List<Medication>) {
        val json = gson.toJson(medications)
        sharedPreferences.edit().putString(MEDICATIONS_KEY, json).apply()
        _medications.value = medications
    }

    override suspend fun addMedication(medication: Medication) {
        val currentMedications = loadMedications().toMutableList()
        val newMedication = medication.copy(id = (currentMedications.maxOfOrNull { it.id } ?: 0) + 1)
        currentMedications.add(newMedication)
        saveMedications(currentMedications)
    }

    override suspend fun updateMedication(medication: Medication) {
        val currentMedications = loadMedications().toMutableList()
        val index = currentMedications.indexOfFirst { it.id == medication.id }
        if (index != -1) {
            currentMedications[index] = medication
            saveMedications(currentMedications)
        }
    }

    override suspend fun getMedication(id: Int): Medication? {
        return loadMedications().find { it.id == id }
    }
}
