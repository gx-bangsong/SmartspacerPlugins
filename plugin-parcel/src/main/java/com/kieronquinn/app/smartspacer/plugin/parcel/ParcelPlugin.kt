package com.kieronquinn.app.smartspacer.plugin.parcel

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.parcel.data.ParcelDatabase
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import org.koin.dsl.module

class ParcelPlugin : SmartspacerPlugin() {
    override fun getModule(context: Context) = module {
        single { ParcelDatabase.getInstance(get()).parcelDao() }
        single { ParcelDatabase.getInstance(get()).ruleDao() }
    }
}
