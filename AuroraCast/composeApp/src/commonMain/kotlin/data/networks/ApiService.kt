package data.networks

import data.models.WeatherResource
import dev.icerock.moko.geo.LatLng
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

    suspend fun getWeather(location: LatLng): WeatherResource {
        return httpClient.get("https://api.openweathermap.org/data/2.5/weather?") {
            parameter("appid", API_KEY)
            parameter("units", "metric")
            parameter("lon", location.longitude)
            parameter("lat", location.latitude)
        }.body()
    }

}