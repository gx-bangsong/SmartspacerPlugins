package com.kieronquinn.app.smartspacer.plugin.food.providers

import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.model.SmartspacerTarget

class FoodProvider : SmartspacerTargetProvider() {

    override fun getTargets(smartspacerId: String): List<SmartspacerTarget> {
        // Logic to create and return the food shelf life target will be added here
        return emptyList()
    }

}