package ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import auroracast.composeapp.generated.resources.Res
import auroracast.composeapp.generated.resources.ic_down
import auroracast.composeapp.generated.resources.ic_humidity
import auroracast.composeapp.generated.resources.ic_location
import auroracast.composeapp.generated.resources.ic_notification
import auroracast.composeapp.generated.resources.ic_wind
import data.models.WeatherResource
import dev.icerock.moko.geo.compose.BindLocationTrackerEffect
import dev.icerock.moko.geo.compose.LocationTrackerAccuracy
import dev.icerock.moko.geo.compose.rememberLocationTrackerFactory
import dev.icerock.moko.permissions.PermissionState
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import ui.forecast.getImage

@Composable
fun HomeScreen(navController: NavController) {
    val factory = rememberLocationTrackerFactory(LocationTrackerAccuracy.Best)
    val locationTracker = remember { factory.createLocationTracker() }
    val viewModel = viewModel { WeatherViewModel(locationTracker) }
    BindLocationTrackerEffect(locationTracker)
    val state = viewModel.state.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val permissionState = viewModel.permissionState.collectAsState()
        when (permissionState.value) {
            PermissionState.Granted -> {
                LaunchedEffect(key1 = Unit) {
                    viewModel.updateLocationData()
                }
                when (state.value) {
                    is AppState.Loading -> {
                        CircularProgressIndicator()
                        Text(text = "Loading...")

                    }

                    is AppState.Success -> {
                        val weather = (state.value as AppState.Success).data
                        HomeScreenContent(weather, navController, viewModel)
                    }

                    is AppState.Error -> {
                        val message = (state.value as AppState.Error).message
                        Text(text = message)
                    }

                    is AppState.GeoCity -> {
                    }
                }
            }

            PermissionState.DeniedAlways -> {
                Button(onClick = {
                    locationTracker.permissionsController.openAppSettings()
                }) {
                    Text(text = "Grant Permission")
                }
            }

            else -> {
                Button(onClick = {
                    viewModel.provideLocationPermission()
                }) {
                    Text(text = "Grant Permission")
                }
            }
        }
    }
}

@Composable
fun HomeScreenContent(
    weather: WeatherResource,
    navController: NavController,
    viewModel: WeatherViewModel
) {
    val searchQuery = remember { mutableStateOf(weather.name!!) }
    val suggestions = remember { mutableStateOf<List<String>>(emptyList()) }

    Box(
        modifier = Modifier.fillMaxSize().background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF7FD4FF), Color(0xFF4A90E2)
                )
            )
        ).systemBarsPadding()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().align(Alignment.TopStart).padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
            ) {
                Box(
                    modifier = Modifier.size(50.dp, 70.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_location),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color.White
                    )
                }
                Column(
                    modifier = Modifier.fillMaxWidth().zIndex(1f)
                ) {
                    OutlinedTextField(
                        value = searchQuery.value,
                        onValueChange = { query ->
                            searchQuery.value = query
                            viewModel.getCitySuggestions(query)
                            suggestions.value = viewModel.cityFilter.value
                        },
                        label = { Text("Select Location", color = Color.White) },
                        singleLine = true,
                        textStyle = TextStyle(color = Color.White),
                        trailingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.White)
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = Color.White,
                            backgroundColor = Color.Transparent,
                            cursorColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    suggestions.value.takeIf { it.isNotEmpty() }?.let { list ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(8.dp)
                                .zIndex(1f)
                        ) {
                            list.forEach { city ->
                                Text(
                                    text = city,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            searchQuery.value = city
                                            suggestions.value = emptyList()
                                            viewModel.fetchWeatherForCity(city)
                                        }
                                        .padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        Column(
            modifier = Modifier.fillMaxSize().align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.size(16.dp))
            Image(
                painter = painterResource(getImage((weather.weather.getOrNull(0)?.main ?: ""))),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.size(32.dp))

            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth()
                    .clip(shape = RoundedCornerShape(16.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.6f))
                    .background(color = Color.White.copy(alpha = 0.4f)).padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "${weather.main?.temp?.toInt()}°",
                    style = MaterialTheme.typography.h2.copy(
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                    ),
                    fontSize = 80.sp
                )
                Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = weather.weather.getOrNull(0)?.description?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                        ?: "",
                    style = MaterialTheme.typography.h6.copy(
                        color = Color.White, fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.size(20.dp))
                WeatherInfoItem(
                    image = Res.drawable.ic_wind,
                    title = "Wind",
                    value = "${weather.wind?.speed} m/s"
                )
                Spacer(modifier = Modifier.size(20.dp))
                WeatherInfoItem(
                    image = Res.drawable.ic_humidity,
                    title = "Hum",
                    value = "${weather.main?.humidity}%"
                )
                Spacer(modifier = Modifier.size(20.dp))
            }
        }
        Button(
            onClick = {
                navController.navigate("forecast?city=${weather.name.toString()}")
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.White)
        ) {
            Text(
                text = "Forecast report",
                color = Color.Black,
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
            )
        }

    }
}

@Composable
fun WeatherInfoItem(image: DrawableResource, title: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(text = title, color = Color.White)
        Spacer(modifier = Modifier.size(8.dp))
        Text("|", color = Color.White)
        Spacer(modifier = Modifier.size(8.dp))
        Text(text = value, color = Color.White)
    }
}
