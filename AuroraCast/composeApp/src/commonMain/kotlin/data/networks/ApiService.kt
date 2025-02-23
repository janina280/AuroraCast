package data.networks

import data.models.WeatherResource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class ApiService {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                coerceInputValues = true
                ignoreUnknownKeys = true
            })
        }
    }
    suspend fun getWeatherByLatLong(lat: Double, lon: Double):WeatherResource {
        return httpClient.get("https://api.openweathermap.org/data/2.5/weather?")
        {
            parameter("lat", lat)
            parameter("lon", lon)
            parameter("appid", API_KEY)
        }.body()
    }

    suspend fun getWeatherByCity(city: String): WeatherResource {
        return httpClient.get("https://api.openweathermap.org/data/2.5/weather?")
        {
            parameter("q", city)
            parameter("appid", API_KEY)
        }.body()
    }

}