package com.kieronquinn.app.smartspacer.plugin.parcel.ui.fragments

import androidx.lifecycle.ViewModel
import com.kieronquinn.app.smartspacer.plugin.parcel.data.ParcelDao
import com.kieronquinn.app.smartspacer.plugin.parcel.data.RuleDao
import com.kieronquinn.app.smartspacer.plugin.parcel.engine.InboxScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class SettingsViewModel : ViewModel() {
    abstract val state: StateFlow<State>
    abstract fun onScanInboxClicked()
    abstract fun onImportRulesClicked()

    sealed class State {
        object Loading : State()
        object Loaded : State()
    }
}

class SettingsViewModelImpl(
    private val parcelDao: ParcelDao,
    private val ruleDao: RuleDao
) : SettingsViewModel() {
    override val state = MutableStateFlow<State>(State.Loaded)

    override fun onScanInboxClicked() {
        // Handled in fragment for permissions
    }

    override fun onImportRulesClicked() {
        // Handled in fragment for picker
    }
}
