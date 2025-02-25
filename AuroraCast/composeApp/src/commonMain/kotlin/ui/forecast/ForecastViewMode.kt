package ui.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.models.ForecastData
import data.models.ForecastResponse
import data.repository.WeatherRepository
import dev.icerock.moko.geo.LatLng
import dev.icerock.moko.geo.LocationTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ForecastViewMode(private val locationTracker: LocationTracker):ViewModel() {
    private val repository=WeatherRepository()
    private val _state= MutableStateFlow<ForecastState>(ForecastState.Loading)
    val state=_state.asStateFlow()

    fun getForecast(){
        viewModelScope.launch {
            _state.value=ForecastState.Loading
            val location=getCurrentLocation()
            try {
                val response=repository.fetchForecast(location)
                val dailyData=getDailyForecast(response)
                val weeklyData=getWeeklyForecast(response).map{it.value.first()}
                _state.value=ForecastState.Data(dailyData, weeklyData)
            }
            catch (e: Exception){
                e.printStackTrace()
                _state.value=ForecastState.Error(e)
            }
        }
    }

    private suspend fun getCurrentLocation():LatLng {
        locationTracker.startTracking()
        val location=locationTracker.getLocationsFlow().first()
        locationTracker.stopTracking()
        return location
    }

    private fun getWeeklyForecast(response: ForecastResponse): Map<String, List<ForecastData>>{
        val groupedData=response.list.sortedBy { it.dt }.groupBy { it.dt_txt.split(" ")[0] }
        return groupedData
    }

    private fun getDailyForecast(response: ForecastResponse): List<ForecastData>? {
        val sortedData=response.list.sortedBy { it.dt }.map { it.dt_txt.split(" ")[0] }
        val groupedData=response.list.groupBy { it.dt_txt.split(" ")[0] }
        return groupedData[sortedData[0]]?: emptyList()
    }
}

sealed class ForecastState{
    object Loading:ForecastState()
    data class Data(val dailyData: List<ForecastData>?, val weeklyDay: List<ForecastData>):ForecastState()
    data class Error(val error: Throwable): ForecastState()
}