package com.kieronquinn.app.smartspacer.plugin.parcel.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.parcel.data.ParcelDao
import com.kieronquinn.app.smartspacer.plugin.parcel.providers.ParcelTargetProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ParcelActionReceiver : BroadcastReceiver(), KoinComponent {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val parcelDao by inject<ParcelDao>()

    override fun onReceive(context: Context, intent: Intent) {
        val parcelId = intent.getLongExtra(EXTRA_PARCEL_ID, -1L)
        if (parcelId == -1L) return

        when (intent.action) {
            ACTION_MARK_AS_PICKED_UP -> {
                scope.launch {
                    parcelDao.markAsPickedUp(parcelId)
                    SmartspacerTargetProvider.notifyChange(context, ParcelTargetProvider::class.java)
                }
            }
        }
    }

    companion object {
        const val ACTION_MARK_AS_PICKED_UP = "com.kieronquinn.app.smartspacer.plugin.parcel.ACTION_MARK_AS_PICKED_UP"
        const val EXTRA_PARCEL_ID = "extra_parcel_id"
    }
}
