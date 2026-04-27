package com.patocodes.geoalarm.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.patocodes.geoalarm.data.AlarmZoneRepository
import com.patocodes.geoalarm.ui.theme.GeoAlarmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GeoAlarmTheme {
                val repository = remember {
                    AlarmZoneRepository(applicationContext)
                }
                Surface(color = MaterialTheme.colorScheme.background) {
                    GeoAlarmNavHost(
                        repository = repository,
                        modifier = Modifier,
                    )
                }
            }
        }
    }
}
