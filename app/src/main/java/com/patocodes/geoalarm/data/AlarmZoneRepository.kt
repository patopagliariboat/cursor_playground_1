package com.patocodes.geoalarm.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.patocodes.geoalarm.domain.AlarmZone
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.alarmDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "alarm_zone",
)

class AlarmZoneRepository(
    private val context: Context,
) {

    val zone: Flow<AlarmZone> = context.alarmDataStore.data.map { prefs ->
        val lat = prefs[Keys.latitude]?.toDoubleOrNull() ?: DefaultLat
        val lon = prefs[Keys.longitude]?.toDoubleOrNull() ?: DefaultLon
        AlarmZone(
            latitude = lat,
            longitude = lon,
            radiusMeters = prefs[Keys.radiusMeters] ?: DefaultRadiusM,
            armed = prefs[Keys.armed] ?: false,
        )
    }

    suspend fun updateZone(
        latitude: Double,
        longitude: Double,
        radiusMeters: Float,
        armed: Boolean,
    ) {
        context.alarmDataStore.edit { prefs ->
            prefs[Keys.latitude] = latitude.toString()
            prefs[Keys.longitude] = longitude.toString()
            prefs[Keys.radiusMeters] = radiusMeters
            prefs[Keys.armed] = armed
        }
    }

    private object Keys {
        val latitude = stringPreferencesKey("latitude")
        val longitude = stringPreferencesKey("longitude")
        val radiusMeters = floatPreferencesKey("radius_meters")
        val armed = booleanPreferencesKey("armed")
    }

    private companion object {
        const val DefaultLat = -34.6037
        const val DefaultLon = -58.3816
        const val DefaultRadiusM = 300f
    }
}
