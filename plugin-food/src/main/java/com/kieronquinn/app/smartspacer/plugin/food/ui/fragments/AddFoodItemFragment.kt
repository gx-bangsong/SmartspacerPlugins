package com.kieronquinn.app.smartspacer.plugin.food.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import com.kieronquinn.app.smartspacer.plugin.food.data.FoodItem

@Composable
fun FoodTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (androidx.compose.foundation.isSystemInDarkTheme()) darkColorScheme() else lightColorScheme(),
        content = content
    )
}

class AddFoodItemFragment : DialogFragment() {

    private var listener: ((FoodItem) -> Unit)? = null

    fun setOnFoodItemAddedListener(listener: (FoodItem) -> Unit) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                FoodTheme {
                    AddFoodItemDialog(
                        onDismiss = { dismiss() },
                        onSave = { foodItem ->
                            listener?.invoke(foodItem)
                            dismiss()
                        }
                    )
                }
            }
        }
    }
}
