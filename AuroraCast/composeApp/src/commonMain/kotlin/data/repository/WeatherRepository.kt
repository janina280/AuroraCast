package data.repository

import data.networks.ApiService

class WeatherRepository {
    private val apiService=ApiService()

    suspend fun getWeatherByLatLong(lat: Double, lon: Double) = apiService.getWeatherByLatLong(lat, lon)
    suspend fun getWeatherByCity(city: String) = apiService.getWeatherByCity(city)

}