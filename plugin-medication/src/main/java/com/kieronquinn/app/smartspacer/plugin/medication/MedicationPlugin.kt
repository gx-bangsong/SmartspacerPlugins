package com.kieronquinn.app.smartspacer.plugin.medication

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.medication.data.MedicationDatabase
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import org.koin.dsl.module

class MedicationPlugin: SmartspacerPlugin() {

    override fun getModule(context: Context) = module {
        single { MedicationDatabase.getDatabase(get()).medicationDao() }
    }

}