package com.fittrack.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fittrack.app.data.FitTrackRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PremiumViewModel(private val repository: FitTrackRepository, private val userId: Long) : ViewModel() {

    val isPremium: StateFlow<Boolean> = repository.getPremiumStatus(userId)
        .map { it?.isPremium ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** Llamado por BillingManager cuando Play confirma la compra (o al restaurarla). */
    fun markPremium(isPremium: Boolean) {
        viewModelScope.launch { repository.setPremiumStatus(userId, isPremium) }
    }
}
