package com.patocodes.geoalarm.ui

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.patocodes.geoalarm.R
import com.patocodes.geoalarm.location.LocationPermissionState

/**
 * Status + quick actions for location so the user can fix denials after arming.
 */
@Composable
fun ArmedLocationInfo(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var permVersion by remember { mutableStateOf(0) }

    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val obs = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permVersion++
            }
        }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs) }
    }

    val fg = remember(permVersion) { LocationPermissionState.hasForegroundAccess(context) }
    val bg = remember(permVersion) { LocationPermissionState.hasBackgroundAccess(context) }

    val fgLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permVersion++ }

    val bgLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { permVersion++ }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(
                if (fg) R.string.armed_perm_foreground_ok else R.string.armed_perm_foreground_missing,
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(
                if (bg) R.string.armed_perm_background_ok else R.string.armed_perm_background_missing,
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (!fg) {
            Button(
                onClick = {
                    fgLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        ),
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            ) {
                Text(stringResource(R.string.armed_perm_grant_location))
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !bg) {
            Button(
                onClick = {
                    bgLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            ) {
                Text(stringResource(R.string.armed_perm_grant_background))
            }
        }
    }
}
