package com.kieronquinn.app.smartspacer.plugin.food.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kieronquinn.app.smartspacer.plugin.food.data.FoodItem
import com.kieronquinn.app.smartspacer.plugin.food.databinding.ItemFoodBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FoodAdapter(private val foodItems: List<FoodItem>) :
    RecyclerView.Adapter<FoodAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFoodBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val foodItem = foodItems[position]
        holder.bind(foodItem)
    }

    override fun getItemCount() = foodItems.size

    class ViewHolder(private val binding: ItemFoodBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        fun bind(foodItem: FoodItem) {
            binding.textViewFoodName.text = foodItem.name
            binding.textViewExpiryDate.text = "Expires on: ${dateFormat.format(Date(foodItem.expiryDate))}"
        }
    }
}
