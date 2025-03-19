package ui.forecast

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import auroracast.composeapp.generated.resources.Res
import auroracast.composeapp.generated.resources.ic_cloud
import auroracast.composeapp.generated.resources.ic_cloud_simple
import auroracast.composeapp.generated.resources.ic_rain
import auroracast.composeapp.generated.resources.ic_sun
import auroracast.composeapp.generated.resources.ic_thunderstorm
import data.models.ForecastData
import dev.icerock.moko.geo.compose.BindLocationTrackerEffect
import dev.icerock.moko.geo.compose.LocationTrackerAccuracy
import dev.icerock.moko.geo.compose.rememberLocationTrackerFactory
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun ForecastScreen(navController: NavController, city: String) {
    val viewMode: ForecastViewMode= viewModel{ForecastViewMode(city)}

    val state=viewMode.state.collectAsState()
    LaunchedEffect(Unit){
        viewMode.getForecast()
    }

    Column (
        modifier = Modifier.fillMaxSize().background(
            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF7FD4FF),
                    Color(0xFF4A90E2)
                )
            )
        ).systemBarsPadding(),
    )
    {
        Row (
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ){
            IconButton(onClick = {navController.popBackStack()}){
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = Color.White
                )
            }
            Text(text = "Back", style = MaterialTheme.typography.h5.copy(
                    color = Color.White
            ))

        }

        when(val forecastState=state.value){
            is ForecastState.Loading->{
                Column (
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    CircularProgressIndicator(color = Color.Blue)
                    Text(
                        text="Loading...",
                        color = Color.White
                    )
                }
            }

            is ForecastState.Data -> {
                val dailyData=forecastState.dailyData
                val weeklyData=forecastState.weeklyDay
                if (dailyData != null) {
                    ForecastScreenContent(dailyData, weeklyData)
                }

            }
            is ForecastState.Error -> {
                Text(text = "Error occurred")
            }
        }
    }
}

@Composable
fun ColumnScope.ForecastScreenContent(
    dailyData: List<ForecastData>,
    weeklyData: List<ForecastData>
){
    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            text = "Daily Forecast",
            modifier = Modifier.align(Alignment.CenterStart),
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = dailyData[0].dt_txt.split(" ")[0],
            modifier = Modifier.align(Alignment.CenterEnd),
            color = Color.White
        )
    }
    LazyRow {
        items(dailyData) { data ->
            ForecastRowItem(data)
        }
    }
    Spacer(modifier = Modifier.size(16.dp))
    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            text = "Daily Forecast",
            modifier = Modifier.align(Alignment.CenterStart),
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = dailyData[0].dt_txt.split(" ")[0],
            modifier = Modifier.align(Alignment.CenterEnd),
            color = Color.White
        )
    }
    LazyColumn {
        items(weeklyData) { data ->
            ForecastColumnItem(data)
        }
    }
    Spacer(modifier = Modifier.size(16.dp))
}


@Composable
fun ForecastRowItem(data: ForecastData) {
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
            .height(155.dp)
    ) {
        Text(text = "${data.main.temp?.toInt()}C", color = Color.White)
        Spacer(modifier = Modifier.size(8.dp))
        Image(
            painter = painterResource(getImage(data.weather.getOrNull(0)?.main ?: "")),
            contentDescription = null,
            modifier = Modifier.size(60.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(text = data.dt_txt.split(" ")[1].removeSuffix(":00"), color = Color.White)
    }
}


fun getImage(data: String): DrawableResource {
    return if (data.lowercase().contains("rain")) {
        Res.drawable.ic_rain
    } else if (data.lowercase().contains("cloud_simple")) {
        Res.drawable.ic_cloud_simple
    }
    else if (data.lowercase().contains("sun")) {
        Res.drawable.ic_sun
    }
    else if (data.lowercase().contains("thunderstorm")) {
        Res.drawable.ic_thunderstorm
    }
    else {
        Res.drawable.ic_cloud
    }
}

@Composable
fun ForecastColumnItem(data: ForecastData) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().height(60.dp)
    ) {
        Text(text = data.dt_txt.split(" ")[0].drop(5), color = Color.White)
        Spacer(modifier = Modifier.size(8.dp))
        Image(
            painter = painterResource(getImage(data.weather.getOrNull(0)?.main ?: "")),
            contentDescription = null,
            modifier = Modifier.size(60.dp),
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(text = "${data.main.temp?.toInt()}C", color = Color.White)
    }
}
