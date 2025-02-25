package org.auroracast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import auroracast.composeapp.generated.resources.Res
import org.jetbrains.compose.ui.tooling.preview.Preview
import dev.icerock.moko.geo.compose.BindLocationTrackerEffect
import dev.icerock.moko.geo.compose.LocationTrackerAccuracy
import dev.icerock.moko.geo.compose.rememberLocationTrackerFactory
import dev.icerock.moko.permissions.PermissionState
import org.jetbrains.compose.resources.painterResource
import ui.AppState
import ui.WeatherViewModel

@Composable
@Preview
fun App() {
    MaterialTheme {
        val factory = rememberLocationTrackerFactory(LocationTrackerAccuracy.Best)
        val locationTracker = remember { factory.createLocationTracker() }
        val viewModel = viewModel { WeatherViewModel(locationTracker) }

        BindLocationTrackerEffect(locationTracker)

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val permissionState = viewModel.permissionState.collectAsState()
            val appState=viewModel.appState.collectAsState()
            when (permissionState.value)
            {
                PermissionState.Granted -> {
                    LaunchedEffect(key1 = Unit) {
                        viewModel.updateLocationData()
                    }
                    when (appState.value) {
                        is AppState.Error -> {
                            val message = (appState.value as AppState.Error).message
                            Text(text = message)
                        }
                        AppState.Loading -> {
                            CircularProgressIndicator( )
                            Text(text = "Loading")
                        }
                        is AppState.Success -> {
                            val data = (appState.value as AppState.Success).data
                            Box(
                                modifier = Modifier.fillMaxSize().background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF000000),
                                            Color(0xFF4A90E2)
                                        )
                                    )
                                )
                            )
                            {
                                Row (horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth().align(Alignment.TopStart).padding(16.dp)){
                                    Text(text = data.name, color = Color.White)
                                    Icon(
                                       imageVector =Icons.Filled.Notifications,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)

                                    )
                                }
                                Column(modifier = Modifier.fillMaxSize().align(Alignment.Center),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally)
                                {
                                    Spacer(modifier = Modifier.size(16.dp))

                                }

                            }
                        }
                    }
                }
                PermissionState.DeniedAlways -> {
                    Button(onClick = { locationTracker.permissionsController.openAppSettings() })
                    {
                        Text(text = "Open App Settings")
                    }
                }
                else -> {
                    Button(onClick = { viewModel.provideLocationPerMission() }) {
                        Text(text = "Grant Permission")
                    }
                }
            }
        }

    }
}



