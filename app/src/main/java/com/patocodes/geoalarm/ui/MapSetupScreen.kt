package com.patocodes.geoalarm.ui

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.patocodes.geoalarm.BuildConfig
import com.patocodes.geoalarm.R
import com.patocodes.geoalarm.domain.AlarmZone
import com.patocodes.geoalarm.location.LocationPermissionState

@Composable
fun MapSetupScreen(
    viewModel: GeoAlarmViewModel,
    onActivate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val zone by viewModel.zone.collectAsStateWithLifecycle()
    var showMapLocationRationale by remember { mutableStateOf(false) }
    var locationPermVersion by remember { mutableStateOf(0) }

    val hasForeground = remember(locationPermVersion) {
        LocationPermissionState.hasForegroundAccess(context)
    }

    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val obs = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                locationPermVersion++
            }
        }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs) }
    }
    val fused = remember { LocationServices.getFusedLocationProviderClient(context) }

    val mapForegroundPerms = remember {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    }
    val mapLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { _ ->
        locationPermVersion++
        if (LocationPermissionState.hasForegroundAccess(context)) {
            moveMapToDeviceLocation(
                context = context,
                fused = fused,
                onLocation = { lat, lon -> viewModel.setPin(lat, lon) },
            )
        }
    }
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

    if (showMapLocationRationale) {
        AlertDialog(
            onDismissRequest = { showMapLocationRationale = false },
            title = { Text(stringResource(R.string.map_perm_rationale_title)) },
            text = { Text(stringResource(R.string.map_perm_rationale_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showMapLocationRationale = false
                        mapLocationLauncher.launch(mapForegroundPerms)
                    },
                ) {
                    Text(stringResource(R.string.perm_action_continue))
                }
            },
            dismissButton = {
                TextButton(onClick = { showMapLocationRationale = false }) {
                    Text(stringResource(R.string.perm_action_not_now))
                }
            },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (mapApiOk) {
            val mapProperties = remember(hasForeground) {
                MapProperties(isMyLocationEnabled = hasForeground)
            }
            GoogleMap(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                properties = mapProperties,
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
            OutlinedButton(
                onClick = {
                    if (hasForeground) {
                        moveMapToDeviceLocation(
                            context = context,
                            fused = fused,
                            onLocation = { lat, lon -> viewModel.setPin(lat, lon) },
                        )
                    } else {
                        showMapLocationRationale = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            ) {
                Text(stringResource(R.string.map_action_my_location))
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

/** One-shot last known / current location to center the map pin. */
private fun moveMapToDeviceLocation(
    context: android.content.Context,
    fused: com.google.android.gms.location.FusedLocationProviderClient,
    onLocation: (Double, Double) -> Unit,
) {
    if (!LocationPermissionState.hasForegroundAccess(context)) {
        return
    }
    val cts = CancellationTokenSource()
    fused.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
        .addOnSuccessListener { loc ->
            if (loc != null) {
                onLocation(loc.latitude, loc.longitude)
            } else {
                fused.lastLocation.addOnSuccessListener { last ->
                    if (last != null) {
                        onLocation(last.latitude, last.longitude)
                    }
                }
            }
        }
}
