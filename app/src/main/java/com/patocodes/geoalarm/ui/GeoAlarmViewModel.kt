package com.patocodes.geoalarm.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.patocodes.geoalarm.data.AlarmZoneRepository
import com.patocodes.geoalarm.domain.AlarmZone
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GeoAlarmViewModel(
    private val repository: AlarmZoneRepository,
) : ViewModel() {

    val zone: StateFlow<AlarmZone> = repository.zone.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AlarmZone(
            latitude = -34.6037,
            longitude = -58.3816,
            radiusMeters = 300f,
            armed = false,
        ),
    )

    fun setPin(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val z = repository.zone.first()
            repository.updateZone(latitude, longitude, z.radiusMeters, z.armed)
        }
    }

    fun setRadiusMeters(radius: Float) {
        viewModelScope.launch {
            val z = repository.zone.first()
            repository.updateZone(z.latitude, z.longitude, radius, z.armed)
        }
    }

    fun arm(onComplete: () -> Unit) {
        viewModelScope.launch {
            val z = repository.zone.first()
            repository.updateZone(z.latitude, z.longitude, z.radiusMeters, armed = true)
            onComplete()
        }
    }

    fun disarm(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            val z = repository.zone.first()
            repository.updateZone(z.latitude, z.longitude, z.radiusMeters, armed = false)
            onComplete?.invoke()
        }
    }

    companion object {
        fun factory(repository: AlarmZoneRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass == GeoAlarmViewModel::class.java)
                    return GeoAlarmViewModel(repository) as T
                }
            }
    }
}
