package com.kieronquinn.app.smartspacer.plugin.parcel.ui.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.smartspacer.plugin.parcel.R
import com.kieronquinn.app.smartspacer.plugin.parcel.data.ParcelDao
import com.kieronquinn.app.smartspacer.plugin.parcel.databinding.FragmentParcelDetailBinding
import com.kieronquinn.app.smartspacer.plugin.parcel.providers.ParcelTargetProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class ParcelDetailFragment : DialogFragment() {

    private var _binding: FragmentParcelDetailBinding? = null
    private val binding get() = _binding!!
    private val parcelDao by inject<ParcelDao>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentParcelDetailBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val parcelId = requireArguments().getLong("parcelId", -1L)
        if (parcelId == -1L) {
            dismiss()
            return
        }

        lifecycleScope.launch {
            val parcel = runCatching { parcelDao.getPendingParcelsList().find { it.id == parcelId } }.getOrNull()
            if (parcel == null) {
                dismiss()
                return@launch
            }

            binding.tvPickupCode.text = parcel.pickupCode
            binding.tvLocation.text = parcel.stationName
            binding.tvRawText.text = parcel.rawText

            binding.tvPickupCode.setOnClickListener {
                copyToClipboard(parcel.pickupCode)
            }

            binding.btnMarkPickedUp.setOnClickListener {
                lifecycleScope.launch {
                    parcelDao.markAsPickedUp(parcelId)
                    SmartspacerTargetProvider.notifyChange(requireContext(), ParcelTargetProvider::class.java)
                    dismiss()
                }
            }
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Pickup Code", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Pickup code copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
