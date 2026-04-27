package com.patocodes.geoalarm.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.patocodes.geoalarm.BuildConfig
import com.patocodes.geoalarm.R
import com.patocodes.geoalarm.domain.AlarmZone

@Composable
fun MapSetupScreen(
    viewModel: GeoAlarmViewModel,
    onActivate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val zone by viewModel.zone.collectAsStateWithLifecycle()
    val markerState = remember(zone.latitude, zone.longitude) {
        MarkerState(LatLng(zone.latitude, zone.longitude))
    }

    LaunchedEffect(zone.latitude, zone.longitude) {
        val next = LatLng(zone.latitude, zone.longitude)
        if (markerState.position != next) {
            markerState.position = next
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(zone.latitude, zone.longitude),
            14f,
        )
    }

    LaunchedEffect(zone.latitude, zone.longitude) {
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(zone.latitude, zone.longitude),
                cameraPositionState.position.zoom,
            ),
        )
    }

    val mapApiOk = BuildConfig.MAPS_API_KEY.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (mapApiOk) {
            GoogleMap(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    viewModel.setPin(latLng.latitude, latLng.longitude)
                },
            ) {
                Marker(
                    state = markerState,
                    title = stringResource(R.string.map_pin_title),
                )
            }
        } else {
            NoMapFallback(
                zone = zone,
                onApplyCoords = { lat, lon -> viewModel.setPin(lat, lon) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
            )
        }

        RadiusSection(
            zone = zone,
            onRadiusChange = viewModel::setRadiusMeters,
            onActivate = onActivate,
        )
    }
}

@Composable
private fun RadiusSection(
    zone: AlarmZone,
    onRadiusChange: (Float) -> Unit,
    onActivate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val presets = remember { listOf(100f, 300f, 500f) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.radius_label, zone.radiusMeters.toInt()),
            style = MaterialTheme.typography.titleMedium,
        )
        Slider(
            value = zone.radiusMeters,
            onValueChange = onRadiusChange,
            valueRange = 50f..2000f,
            steps = 38,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
            ),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            presets.forEach { m ->
                FilterChip(
                    selected = zone.radiusMeters == m,
                    onClick = { onRadiusChange(m) },
                    label = { Text(stringResource(R.string.radius_meters_short, m.toInt())) },
                )
            }
        }
        Text(
            text = stringResource(
                R.string.coords_line,
                zone.latitude,
                zone.longitude,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        )
        Button(
            onClick = onActivate,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.action_activate))
        }
    }
}

@Composable
private fun NoMapFallback(
    zone: AlarmZone,
    onApplyCoords: (Double, Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    var latText by remember { mutableStateOf("") }
    var lonText by remember { mutableStateOf("") }
    LaunchedEffect(zone.latitude, zone.longitude) {
        latText = String.format("%1.5f", zone.latitude)
        lonText = String.format("%1.5f", zone.longitude)
    }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.map_needs_api_key),
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            value = latText,
            onValueChange = { latText = it },
            label = { Text(stringResource(R.string.label_latitude)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = lonText,
            onValueChange = { lonText = it },
            label = { Text(stringResource(R.string.label_longitude)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = {
                val lat = latText.replace(",", ".").toDoubleOrNull()
                val lon = lonText.replace(",", ".").toDoubleOrNull()
                if (lat != null && lon != null) {
                    onApplyCoords(lat, lon)
                }
            },
            enabled = latText.isNotBlank() && lonText.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.action_apply_coords))
        }
    }
}
