package ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.models.LocationResource
import data.models.WeatherResource
import data.repository.WeatherRepository
import dev.icerock.moko.geo.LatLng
import dev.icerock.moko.geo.LocationTracker
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val locationTracker: LocationTracker
) : ViewModel() {

    val repository = WeatherRepository()
    private val _state = MutableStateFlow<AppState>(AppState.Loading)
    val state = _state.asStateFlow()
    var location: LatLng? = null
    private val _permissionState = MutableStateFlow(PermissionState.NotDetermined)
    val permissionState = _permissionState.asStateFlow()

    var cities: List<String> = listOf()
    private val _cityFilter = MutableStateFlow<List<String>>(
        value = cities
    )
    val cityFilter = _cityFilter.asStateFlow()

    init {
        viewModelScope.launch {
            _permissionState.value =
                locationTracker.permissionsController.getPermissionState(Permission.LOCATION)
        }
    }

    private fun fetchWeather(location: LatLng) {
        viewModelScope.launch {
            _state.value = AppState.Loading
            try {
                val result = repository.fetchWeather(location)
                _state.value = AppState.Success(result)
            } catch (e: Exception) {
                println(e.message)
                _state.value = AppState.Error("Failed to load weather")
            }
        }
    }

    fun provideLocationPermission() {
        viewModelScope.launch {
            val isGranted =
                locationTracker.permissionsController.isPermissionGranted(Permission.LOCATION)
            if (isGranted) {
                _permissionState.value = PermissionState.Granted
                return@launch
            }
            try {
                locationTracker.permissionsController.providePermission(
                    Permission.LOCATION
                )
                _permissionState.value = PermissionState.Granted
            } catch (e: DeniedAlwaysException) {
                _permissionState.value = PermissionState.DeniedAlways
            } catch (e: DeniedException) {
                _permissionState.value = PermissionState.Denied
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    fun fetchWeatherForCity(city: String) {
        viewModelScope.launch {
            _state.value = AppState.Loading
            try {
                val location = AppState.GeoCity(repository.fetchCityLocation(city)).data

                fetchWeather(LatLng(location.lat, location.lon))
            } catch (e: Exception) {
                _state.value = AppState.Error("Failed to load weather for $city")
            }
        }
    }

    fun getCitySuggestions(query: String) {
        viewModelScope.launch {
            if (cities.isEmpty()) {
                cities = repository.fetchCities()
                    .map { c ->
                        c.city.lowercase()
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    }
            }
            _cityFilter.value = cities.filter { it.startsWith(query, ignoreCase = true) }
        }
    }

    fun updateLocationData() {
        viewModelScope.launch {
            if (location == null) {
                val latLng = getUserLocation()
                location = latLng
            }
            fetchWeather(location!!)
        }
    }

    private suspend fun getUserLocation(): LatLng {
        locationTracker.startTracking()
        val location = locationTracker.getLocationsFlow().first()
        locationTracker.stopTracking()
        return location
    }


}

sealed class AppState {
    object Loading : AppState()
    data class Success(val data: WeatherResource) : AppState()
    data class Error(val message: String) : AppState()
    data class GeoCity(val data: LocationResource) : AppState()
}