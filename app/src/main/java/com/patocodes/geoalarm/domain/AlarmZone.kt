package com.patocodes.geoalarm.domain

/**
 * Target point and radius for the future geofence (Etapa 3+). No GPS yet in Etapa 1.
 */
data class AlarmZone(
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float,
    val armed: Boolean,
)
