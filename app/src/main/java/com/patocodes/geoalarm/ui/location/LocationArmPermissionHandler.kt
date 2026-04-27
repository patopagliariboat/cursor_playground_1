package com.patocodes.geoalarm.ui.location

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.patocodes.geoalarm.R
import com.patocodes.geoalarm.location.LocationPermissionState
import com.patocodes.geoalarm.ui.GeoAlarmViewModel

/**
 * Rationale + request flow for [Manifest.permission.ACCESS_FINE_LOCATION],
 * [Manifest.permission.ACCESS_COARSE_LOCATION], and (API 29+)
 * [Manifest.permission.ACCESS_BACKGROUND_LOCATION] before [GeoAlarmViewModel.arm].
 * Invokes [content] with [LocationArmHandle.onActivateTapped] wired to the primary action.
 */
@Composable
fun LocationArmPermissionHandler(
    viewModel: GeoAlarmViewModel,
    onArmedNavigation: () -> Unit,
    content: @Composable (handle: LocationArmHandle) -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? androidx.activity.ComponentActivity

    var showForegroundRationale by remember { mutableStateOf(false) }
    var showBackgroundRationale by remember { mutableStateOf(false) }
    var showArmedWithLimited by remember { mutableStateOf(false) }
    var showSettingsForForeground by remember { mutableStateOf(false) }
    var showSettingsForBackground by remember { mutableStateOf(false) }

    val foregroundPermissions = remember {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    }

    val foregroundLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { _ ->
        if (LocationPermissionState.hasForegroundAccess(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                !LocationPermissionState.hasBackgroundAccess(context)
            ) {
                showBackgroundRationale = true
            } else {
                viewModel.arm(onArmedNavigation)
            }
        } else {
            val anyPermanentlyDenied = activity != null && foregroundPermissions.any { perm ->
                !ActivityCompat.shouldShowRequestPermissionRationale(activity, perm) &&
                    isDenied(context, perm)
            }
            if (anyPermanentlyDenied) {
                showSettingsForForeground = true
            }
        }
    }

    val backgroundLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.arm(onArmedNavigation)
        } else {
            val permanent =
                activity != null &&
                !ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                ) &&
                isDenied(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            if (permanent) {
                showSettingsForBackground = true
            } else {
                showArmedWithLimited = true
            }
        }
    }

    val handle = object : LocationArmHandle {
        override fun onActivateTapped() {
            when {
                LocationPermissionState.canArmWithLocationPolicy(context) -> {
                    viewModel.arm(onArmedNavigation)
                }
                !LocationPermissionState.hasForegroundAccess(context) -> {
                    showForegroundRationale = true
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    !LocationPermissionState.hasBackgroundAccess(context) -> {
                    showBackgroundRationale = true
                }
                else -> {
                    viewModel.arm(onArmedNavigation)
                }
            }
        }
    }

    if (showForegroundRationale) {
        AlertDialog(
            onDismissRequest = { showForegroundRationale = false },
            title = { Text(stringResource(R.string.perm_rationale_foreground_title)) },
            text = { Text(stringResource(R.string.perm_rationale_foreground_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showForegroundRationale = false
                        foregroundLauncher.launch(foregroundPermissions)
                    },
                ) {
                    Text(stringResource(R.string.perm_action_continue))
                }
            },
            dismissButton = {
                TextButton(onClick = { showForegroundRationale = false }) {
                    Text(stringResource(R.string.perm_action_not_now))
                }
            },
        )
    }

    if (showBackgroundRationale) {
        AlertDialog(
            onDismissRequest = { showBackgroundRationale = false },
            title = { Text(stringResource(R.string.perm_rationale_background_title)) },
            text = { Text(stringResource(R.string.perm_rationale_background_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBackgroundRationale = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            backgroundLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        } else {
                            viewModel.arm(onArmedNavigation)
                        }
                    },
                ) {
                    Text(stringResource(R.string.perm_action_allow_all_time))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showBackgroundRationale = false
                        showArmedWithLimited = true
                    },
                ) {
                    Text(stringResource(R.string.perm_action_not_now))
                }
            },
        )
    }

    if (showArmedWithLimited) {
        AlertDialog(
            onDismissRequest = { showArmedWithLimited = false },
            title = { Text(stringResource(R.string.perm_limited_title)) },
            text = { Text(stringResource(R.string.perm_limited_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showArmedWithLimited = false
                        viewModel.arm(onArmedNavigation)
                    },
                ) {
                    Text(stringResource(R.string.perm_limited_arm_anyway))
                }
            },
            dismissButton = {
                TextButton(onClick = { showArmedWithLimited = false }) {
                    Text(stringResource(R.string.perm_action_cancel))
                }
            },
        )
    }

    if (showSettingsForForeground) {
        AlertDialog(
            onDismissRequest = { showSettingsForForeground = false },
            title = { Text(stringResource(R.string.perm_settings_title)) },
            text = { Text(stringResource(R.string.perm_settings_foreground_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSettingsForForeground = false
                        openAppSettings(context)
                    },
                ) {
                    Text(stringResource(R.string.perm_action_open_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsForForeground = false }) {
                    Text(stringResource(R.string.perm_action_cancel))
                }
            },
        )
    }

    if (showSettingsForBackground) {
        AlertDialog(
            onDismissRequest = { showSettingsForBackground = false },
            title = { Text(stringResource(R.string.perm_settings_title)) },
            text = { Text(stringResource(R.string.perm_settings_background_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSettingsForBackground = false
                        openAppSettings(context)
                    },
                ) {
                    Text(stringResource(R.string.perm_action_open_settings))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSettingsForBackground = false
                        viewModel.arm(onArmedNavigation)
                    },
                ) {
                    Text(stringResource(R.string.perm_limited_arm_anyway))
                }
            },
        )
    }

    content(handle)
}

interface LocationArmHandle {
    fun onActivateTapped()
}

private fun isDenied(context: android.content.Context, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        permission,
    ) != android.content.pm.PackageManager.PERMISSION_GRANTED
}

private fun openAppSettings(context: android.content.Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}
