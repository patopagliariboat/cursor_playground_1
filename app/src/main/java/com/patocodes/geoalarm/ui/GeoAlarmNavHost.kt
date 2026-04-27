package com.patocodes.geoalarm.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.patocodes.geoalarm.data.AlarmZoneRepository
import kotlinx.coroutines.flow.first

@Composable
fun GeoAlarmNavHost(
    repository: AlarmZoneRepository,
    modifier: Modifier = Modifier,
) {
    var startRoute by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(repository) {
        val armed = repository.zone.first().armed
        startRoute = if (armed) Route.Armed else Route.Setup
    }

    if (startRoute == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val navController = rememberNavController()
    val vm: GeoAlarmViewModel = viewModel(factory = GeoAlarmViewModel.factory(repository))

    NavHost(
        navController = navController,
        startDestination = startRoute!!,
        modifier = modifier,
    ) {
        composable(Route.Setup) {
            MapSetupScreen(
                viewModel = vm,
                onActivate = {
                    vm.arm {
                        navController.navigate(Route.Armed) {
                            popUpTo(Route.Setup) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                },
            )
        }
        composable(Route.Armed) {
            ArmedScreen(
                viewModel = vm,
                onDisarm = {
                    vm.disarm {
                        navController.navigate(Route.Setup) {
                            popUpTo(Route.Armed) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                },
            )
        }
    }
}
