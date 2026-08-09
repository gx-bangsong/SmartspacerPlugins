package com.kieronquinn.app.smartspacer.plugin.travel.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.smartspacer.plugin.travel.R
import com.kieronquinn.app.smartspacer.plugin.travel.data.TravelInfoItem
import com.kieronquinn.app.smartspacer.plugin.travel.databinding.FragmentTravelSettingsBinding
import com.kieronquinn.app.smartspacer.plugin.travel.providers.TravelTargetProvider
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.SwitchSetting
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Dropdown
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Header
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseFragment
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.views.LifecycleAwareRecyclerView
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class TravelSettingsFragment : BaseFragment<FragmentTravelSettingsBinding>(FragmentTravelSettingsBinding::inflate) {

    private val viewModel by viewModel<TravelSettingsViewModel>()

    override val adapter by lazy {
        object : BaseSettingsAdapter(recyclerView, emptyList()) {}
    }

    override val recyclerView: LifecycleAwareRecyclerView
        get() = binding.settingsBaseRecyclerView

    override val loadingView: LinearProgressIndicator
        get() = binding.settingsBaseLoadingProgress

    private val requestSmsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.RECEIVE_SMS] == true
        if (granted) {
            Toast.makeText(requireContext(), "短信解析权限已启用", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.setSmsParsingEnabled(false)
            Toast.makeText(requireContext(), "无短信权限，无法启用短信解析", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.settingsBaseLoading.visibility = View.GONE

        setupSettingsAndTrips()
        setupFab()
    }

    private fun setupSettingsAndTrips() {
        lifecycleScope.launch {
            viewModel.allTrips.collect { trips ->
                val isSmsEnabled = viewModel.isSmsParsingEnabled.value
                val isReadNotifEnabled = viewModel.isReadNotificationEnabled.value
                val currentTarget = viewModel.jumpTarget.value

                val appOptions = listOf("none", "12306", "umetrip", "maps")
                val appLabels = mapOf(
                    "none" to getString(R.string.settings_jump_none),
                    "12306" to getString(R.string.settings_jump_12306),
                    "umetrip" to getString(R.string.settings_jump_umetrip),
                    "maps" to getString(R.string.settings_jump_maps)
                )

                val settingsItems = mutableListOf<BaseSettingsItem>(
                    SwitchSetting(
                        checked = isSmsEnabled,
                        title = getString(R.string.settings_enable_sms),
                        subtitle = getString(R.string.settings_enable_sms_summary),
                        icon = ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_smartspacer),
                        onChanged = { enabled ->
                            viewModel.setSmsParsingEnabled(enabled)
                            if (enabled) {
                                checkSmsPermission()
                            }
                        }
                    ),
                    SwitchSetting(
                        checked = isReadNotifEnabled,
                        title = getString(R.string.settings_read_notif),
                        subtitle = getString(R.string.settings_read_notif_summary),
                        icon = ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_info),
                        onChanged = { enabled ->
                            viewModel.setReadNotificationEnabled(enabled)
                        }
                    ),
                    Dropdown(
                        getString(R.string.settings_jump_target),
                        getString(R.string.settings_jump_target_summary),
                        ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_libraries),
                        setting = currentTarget,
                        onSet = { target ->
                            viewModel.setJumpTarget(target)
                            SmartspacerTargetProvider.notifyChange(requireContext(), TravelTargetProvider::class.java)
                        },
                        options = appOptions,
                        adapter = { appLabels[it] ?: it }
                    )
                )

                if (trips.isNotEmpty()) {
                    settingsItems.add(Header(getString(R.string.trip_list_header)))
                }

                for (trip in trips) {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                    val dateStr = sdf.format(java.util.Date(trip.departureTime))
                    val title = if (!trip.arrivalStation.isNullOrEmpty()) {
                        "${trip.trainNumber}  ${trip.departureStation} → ${trip.arrivalStation}"
                    } else {
                        "${trip.trainNumber}  ${trip.departureStation}"
                    }
                    val subtitle = "$dateStr | 座位: ${trip.seat ?: "无"} | 乘车人: ${trip.passengerName ?: "无"}"
                    settingsItems.add(
                        Setting(
                            title,
                            subtitle,
                            ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_close),
                            onClick = {
                                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                    .setTitle(R.string.settings_delete)
                                    .setMessage(R.string.settings_delete_confirm)
                                    .setPositiveButton(android.R.string.ok) { _, _ ->
                                        viewModel.deleteTrip(trip)
                                        SmartspacerTargetProvider.notifyChange(requireContext(), TravelTargetProvider::class.java)
                                        Toast.makeText(requireContext(), "行程已删除", Toast.LENGTH_SHORT).show()
                                    }
                                    .setNegativeButton(android.R.string.cancel, null)
                                    .show()
                            }
                        )
                    )
                }

                adapter.update(settingsItems)
            }
        }
    }

    private fun checkSmsPermission() {
        val permissions = arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
        if (permissions.any { ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED }) {
            requestSmsPermissionLauncher.launch(permissions)
        }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            val fragment = ManualPasteFragment()
            fragment.setOnTravelItemAddedListener { item ->
                viewModel.addTrip(item)
                SmartspacerTargetProvider.notifyChange(requireContext(), TravelTargetProvider::class.java)
            }
            fragment.show(childFragmentManager, "ManualPasteFragment")
        }
    }
}
