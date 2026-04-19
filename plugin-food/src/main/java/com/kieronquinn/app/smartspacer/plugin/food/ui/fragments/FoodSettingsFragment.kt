package com.kieronquinn.app.smartspacer.plugin.food.ui.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.kieronquinn.app.smartspacer.plugin.food.data.FoodItemDao
import com.kieronquinn.app.smartspacer.plugin.food.databinding.FragmentFoodSettingsBinding
import com.kieronquinn.app.smartspacer.plugin.food.ui.adapters.FoodAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseFragment
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.views.LifecycleAwareRecyclerView
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class FoodSettingsFragment : BaseFragment<FragmentFoodSettingsBinding>(FragmentFoodSettingsBinding::inflate) {

    private val foodItemDao by inject<FoodItemDao>()

    override val adapter by lazy {
        object : BaseSettingsAdapter(recyclerView, emptyList()) {}
    }

    override val recyclerView: LifecycleAwareRecyclerView
        get() = binding.settingsBaseRecyclerView

    override val loadingView: LinearProgressIndicator
        get() = binding.settingsBaseLoadingProgress

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFoodList()
        setupFab()
    }

    private fun setupFoodList() {
        lifecycleScope.launch {
            foodItemDao.getAll().collect { foodItems ->
                binding.settingsBaseLoading.isVisible = false
                recyclerView.adapter = object : com.kieronquinn.app.smartspacer.plugin.shared.ui.views.LifecycleAwareRecyclerView.Adapter<FoodAdapter.ViewHolder>(recyclerView) {
                    private val innerAdapter = FoodAdapter(foodItems) { foodItem ->
                        lifecycleScope.launch {
                            foodItemDao.delete(foodItem)
                            com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider.notifyChange(requireContext(), com.kieronquinn.app.smartspacer.plugin.food.providers.FoodProvider::class.java)
                        }
                    }

                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodAdapter.ViewHolder {
                        return innerAdapter.onCreateViewHolder(parent, viewType)
                    }

                    override fun onBindViewHolder(holder: FoodAdapter.ViewHolder, position: Int) {
                        innerAdapter.onBindViewHolder(holder, position)
                    }

                    override fun getItemCount(): Int = innerAdapter.itemCount
                }
            }
        }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            val addFoodItemFragment = AddFoodItemFragment()
            addFoodItemFragment.setOnFoodItemAddedListener { foodItem ->
                lifecycleScope.launch {
                    foodItemDao.insert(foodItem)
                    com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider.notifyChange(requireContext(), com.kieronquinn.app.smartspacer.plugin.food.providers.FoodProvider::class.java)
                }
            }
            addFoodItemFragment.show(childFragmentManager, "AddFoodItemFragment")
        }
    }
}
