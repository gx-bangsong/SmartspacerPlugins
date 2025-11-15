package com.kieronquinn.app.smartspacer.plugin.food.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kieronquinn.app.smartspacer.plugin.food.data.FoodItemDao
import com.kieronquinn.app.smartspacer.plugin.food.databinding.ActivitySettingsBinding
import com.kieronquinn.app.smartspacer.plugin.food.ui.adapters.FoodAdapter
import com.kieronquinn.app.smartspacer.plugin.food.ui.fragments.AddFoodItemFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val foodItemDao by inject<FoodItemDao>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        lifecycleScope.launch {
            foodItemDao.getAll().collect { foodItems ->
                binding.recyclerView.adapter = FoodAdapter(foodItems)
            }
        }

        binding.fabAdd.setOnClickListener {
            val addFoodItemFragment = AddFoodItemFragment()
            addFoodItemFragment.setOnFoodItemAddedListener { foodItem ->
                lifecycleScope.launch {
                    foodItemDao.insert(foodItem)
                }
            }
            addFoodItemFragment.show(supportFragmentManager, "AddFoodItemFragment")
        }
    }

}