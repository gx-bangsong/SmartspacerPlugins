package com.kieronquinn.app.smartspacer.plugin.food.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.food.data.FoodItemDao
import com.kieronquinn.app.smartspacer.plugin.food.providers.FoodProvider
import com.kieronquinn.app.smartspacer.plugin.food.repositories.FoodScheduler
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.verifySecurity
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.applySecurity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FoodAlarmReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        private const val EXTRA_FOOD_ITEM_ID = "food_item_id"

        fun createIntent(context: Context, foodItemId: Int): Intent {
            return Intent(context, FoodAlarmReceiver::class.java).apply {
                putExtra(EXTRA_FOOD_ITEM_ID, foodItemId)
                applySecurity(context)
            }
        }
    }

    private val foodItemDao by inject<FoodItemDao>()
    private val foodScheduler by inject<FoodScheduler>()

    override fun onReceive(context: Context, intent: Intent) {
        intent.verifySecurity(context)
        val foodItemId = intent.getIntExtra(EXTRA_FOOD_ITEM_ID, -1)
        if (foodItemId == -1) return

        SmartspacerTargetProvider.notifyChange(context, FoodProvider::class.java)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val foodItem = foodItemDao.getById(foodItemId)
                if (foodItem != null) {
                    foodScheduler.scheduleReminder(foodItem)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
