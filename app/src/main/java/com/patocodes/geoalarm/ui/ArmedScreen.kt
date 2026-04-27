package com.patocodes.geoalarm.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patocodes.geoalarm.R

@Composable
fun ArmedScreen(
    viewModel: GeoAlarmViewModel,
    onDisarm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val zone by viewModel.zone.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.armed_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.armed_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.radius_label, zone.radiusMeters.toInt()),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.coords_line, zone.latitude, zone.longitude),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onDisarm,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.action_disarm))
        }
    }
}
