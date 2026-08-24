package com.kieronquinn.app.smartspacer.plugin.water.ui.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kieronquinn.app.smartspacer.plugin.water.R
import com.kieronquinn.app.smartspacer.plugin.water.data.DrinkHistory
import com.kieronquinn.app.smartspacer.plugin.water.data.DrinkHistoryDao
import com.kieronquinn.app.smartspacer.plugin.water.databinding.FragmentRecordDrinkBinding
import com.kieronquinn.app.smartspacer.plugin.water.notifications.WaterLiveUpdatePublisher
import com.kieronquinn.app.smartspacer.plugin.water.providers.WaterProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class RecordDrinkFragment : BottomSheetDialogFragment() {

    private val drinkHistoryDao by inject<DrinkHistoryDao>()
    private val liveUpdatePublisher by inject<WaterLiveUpdatePublisher>()

    private var _binding: FragmentRecordDrinkBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordDrinkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val amount = arguments?.getInt("amount", -1) ?: -1
        if (amount <= 0) {
            closeHost()
            return
        }

        binding.textViewDrinkInfo.text = getString(R.string.water_record_amount, amount)

        binding.buttonTaken.setOnClickListener {
            lifecycleScope.launch {
                drinkHistoryDao.insert(
                    DrinkHistory(timestamp = System.currentTimeMillis(), amount = amount)
                )
                SmartspacerTargetProvider.notifyChange(requireContext(), WaterProvider::class.java)
                runCatching { liveUpdatePublisher.publish() }
                Toast.makeText(requireContext(), R.string.water_record_success, Toast.LENGTH_SHORT).show()
                closeHost()
            }
        }

        binding.buttonSkip.setOnClickListener {
            closeHost()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        activity?.finish()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        activity?.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun closeHost() {
        dismissAllowingStateLoss()
        activity?.finish()
    }
}
