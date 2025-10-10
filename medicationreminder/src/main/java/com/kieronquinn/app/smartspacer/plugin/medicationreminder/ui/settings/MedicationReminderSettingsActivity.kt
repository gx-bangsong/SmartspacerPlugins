package com.kieronquinn.app.smartspacer.plugin.medicationreminder.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kieronquinn.app.smartspacer.plugin.medicationreminder.ui.theme.MedicationReminderTheme

class MedicationReminderSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MedicationReminderTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "settings") {
                    composable("settings") {
                        MedicationReminderSettingsScreen(
                            onAddMedicationClicked = {
                                navController.navigate("add_medication")
                            }
                        )
                    }
                    composable("add_medication") {
                        AddMedicationScreen(
                            onSaveClicked = {
                                navController.popBackStack()
                            },
                            onBackClicked = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}