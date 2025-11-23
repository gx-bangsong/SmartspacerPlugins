package com.kieronquinn.app.smartspacer.plugin.water.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.smartspacer.plugin.water.data.DrinkHistory
import com.kieronquinn.app.smartspacer.plugin.water.data.DrinkHistoryDao
import com.kieronquinn.app.smartspacer.plugin.water.databinding.ActivityRecordDrinkBinding
import com.kieronquinn.app.smartspacer.plugin.water.repositories.WaterDataRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class RecordDrinkActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordDrinkBinding
    private val drinkHistoryDao by inject<DrinkHistoryDao>()
    private val waterDataRepository by inject<WaterDataRepository>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordDrinkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val amount = intent.getIntExtra("amount", -1)
        if (amount == -1) {
            finish()
            return
        }

        binding.textViewDrinkInfo.text = "Drink ${amount}ml"

        binding.buttonTaken.setOnClickListener {
            lifecycleScope.launch {
                val drinkHistory = DrinkHistory(
                    timestamp = System.currentTimeMillis(),
                    amount = amount
                )
                drinkHistoryDao.insert(drinkHistory)
                waterDataRepository.logWaterIntake(amount)
                SmartspacerTargetProvider.notifyChange(this@RecordDrinkActivity, com.kieronquinn.app.smartspacer.plugin.water.providers.WaterProvider::class.java)
                finish()
            }
        }

        binding.buttonSkip.setOnClickListener {
            lifecycleScope.launch {
                // We don't need to do anything here, just close the activity
                finish()
            }
        }
    }
}
