package com.arar.muslim.presentation.qibla

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arar.muslim.domain.model.QiblaData
import com.arar.muslim.domain.repository.QiblaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class QiblaViewModel(
    private val qiblaRepository: QiblaRepository
) : ViewModel() {

    val qiblaState: StateFlow<QiblaData> = qiblaRepository.qiblaState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QiblaData())

    private val _showCalibrationDialog = MutableStateFlow(false)
    val showCalibrationDialog = _showCalibrationDialog.asStateFlow()

    fun onResume() {
        qiblaRepository.startCompass()
    }

    fun onPause() {
        qiblaRepository.stopCompass()
    }

    fun updateCoordinates(lat: Double, lng: Double) {
        qiblaRepository.updateLocation(lat, lng)
    }

    fun setShowCalibrationDialog(show: Boolean) {
        _showCalibrationDialog.value = show
    }
}
