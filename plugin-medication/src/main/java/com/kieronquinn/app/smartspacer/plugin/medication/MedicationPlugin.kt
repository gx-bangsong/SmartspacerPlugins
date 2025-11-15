package com.kieronquinn.app.smartspacer.plugin.medication

import android.content.Context
import com.google.gson.Gson
import com.kieronquinn.app.smartspacer.plugin.medication.data.MedicationRepository
import com.kieronquinn.app.smartspacer.plugin.medication.data.MedicationRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import org.koin.dsl.module

class MedicationPlugin: SmartspacerPlugin() {

    override fun getModule(context: Context) = module {
        single { Gson() }
        single<MedicationRepository> { MedicationRepositoryImpl(get(), get()) }
    }

}