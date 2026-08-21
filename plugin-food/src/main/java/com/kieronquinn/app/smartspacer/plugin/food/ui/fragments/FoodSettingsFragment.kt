package com.kieronquinn.app.smartspacer.plugin.food.ui.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.kieronquinn.app.smartspacer.plugin.food.R
import com.kieronquinn.app.smartspacer.plugin.food.data.FoodItemDao
import com.kieronquinn.app.smartspacer.plugin.food.databinding.FragmentFoodSettingsBinding
import com.kieronquinn.app.smartspacer.plugin.food.repositories.FoodScheduler
import com.kieronquinn.app.smartspacer.plugin.food.ui.adapters.FoodAdapter
import com.kieronquinn.app.smartspacer.plugin.food.work.FoodWorker
import com.kieronquinn.app.smartspacer.plugin.shared.notifications.NotificationIds
import com.kieronquinn.app.smartspacer.plugin.shared.notifications.NotificationPermissionHelper
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseFragment
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.views.LifecycleAwareRecyclerView
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class FoodSettingsFragment : BaseFragment<FragmentFoodSettingsBinding>(FragmentFoodSettingsBinding::inflate) {

    private val foodItemDao by inject<FoodItemDao>()
    private val foodScheduler by inject<FoodScheduler>()

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        updateNotificationPermissionRow()
    }

    override val adapter by lazy {
        object : BaseSettingsAdapter(recyclerView, emptyList()) {}
    }

    override val recyclerView: LifecycleAwareRecyclerView
        get() = binding.settingsBaseRecyclerView

    override val loadingView: LinearProgressIndicator
        get() = binding.settingsBaseLoadingProgress

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.settingsBaseLoading.visibility = View.GONE
        binding.notificationPermissionRow.setOnClickListener {
            if (NotificationPermissionHelper.hasNotificationPermission(requireContext())) {
                NotificationPermissionHelper.openNotificationSettings(requireContext())
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                NotificationPermissionHelper.openNotificationSettings(requireContext())
            }
        }
        updateNotificationPermissionRow()
        setupFoodList()
        setupFab()
        // 确保定期刷新任务已启动
        FoodWorker.enqueuePeriodic(requireContext())
    }

    private fun updateNotificationPermissionRow() {
        binding.notificationPermissionRow.text = if (NotificationPermissionHelper.hasNotificationPermission(requireContext())) {
            getString(R.string.notification_permission_granted)
        } else {
            getString(R.string.notification_permission_denied)
        }
    }

    private fun setupFoodList() {
        lifecycleScope.launch {
            foodItemDao.getAll().collect { foodItems ->
                binding.settingsBaseLoading.visibility = View.GONE
                val foodAdapter = FoodAdapter(foodItems) { foodItem ->
                    lifecycleScope.launch {
                        foodItemDao.delete(foodItem)
                        foodScheduler.cancelReminder(foodItem.id)
                        // 删除物品时同步取消对应提醒通知
                        NotificationManagerCompat.from(requireContext()).cancel(
                            NotificationIds.forEntity(NotificationIds.NAMESPACE_FOOD, foodItem.id.toLong())
                        )
                        // 数据变更时触发立即刷新
                        FoodWorker.enqueueImmediate(requireContext())
                    }
                }
                recyclerView.adapter = object : com.kieronquinn.app.smartspacer.plugin.shared.ui.views.LifecycleAwareRecyclerView.Adapter<FoodAdapter.ViewHolder>(recyclerView) {
                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodAdapter.ViewHolder {
                        return foodAdapter.onCreateViewHolder(parent, viewType)
                    }

                    override fun onBindViewHolder(holder: FoodAdapter.ViewHolder, position: Int) {
                        foodAdapter.onBindViewHolder(holder, position)
                    }

                    override fun getItemCount(): Int = foodAdapter.itemCount
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
                    foodScheduler.rescheduleAll()
                    // 添加新项后即时刷新
                    FoodWorker.enqueueImmediate(requireContext())
                }
            }
            addFoodItemFragment.show(childFragmentManager, "AddFoodItemFragment")
        }
    }
}
