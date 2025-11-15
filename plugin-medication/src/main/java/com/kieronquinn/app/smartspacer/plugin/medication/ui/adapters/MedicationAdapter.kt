package com.kieronquinn.app.smartspacer.plugin.medication.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kieronquinn.app.smartspacer.plugin.medication.data.Medication
import com.kieronquinn.app.smartspacer.plugin.medication.databinding.ItemMedicationBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MedicationAdapter(private val medications: List<Medication>) :
    RecyclerView.Adapter<MedicationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMedicationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val medication = medications[position]
        holder.bind(medication)
    }

    override fun getItemCount() = medications.size

    class ViewHolder(private val binding: ItemMedicationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        fun bind(medication: Medication) {
            binding.textViewMedicationName.text = medication.name
            binding.textViewNextDose.text = "Next dose: ${dateFormat.format(Date(medication.nextDoseTs))}"
        }
    }
}
