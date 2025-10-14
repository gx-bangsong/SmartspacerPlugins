package com.kieronquinn.app.smartspacer.plugin.medication.providers

import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.model.SmartspacerTarget

class MedicationProvider : SmartspacerTargetProvider() {

    override fun getTargets(smartspacerId: String): List<SmartspacerTarget> {
        // Logic to create and return the medication reminder target will be added here
        return emptyList()
    }

}