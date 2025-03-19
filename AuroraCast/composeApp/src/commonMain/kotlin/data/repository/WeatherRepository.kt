package data.repository

import data.networks.ApiService
import dev.icerock.moko.geo.LatLng

class WeatherRepository {
    private val apiService=ApiService()

    suspend fun fetchCities() = apiService.getCities()
    suspend fun fetchCityLocation(city: String) = apiService.getLocation(city)
    suspend fun fetchWeather(location: LatLng) = apiService.getWeather(location)
    suspend fun fetchForecast(location: LatLng) = apiService.getForecast(location)
}