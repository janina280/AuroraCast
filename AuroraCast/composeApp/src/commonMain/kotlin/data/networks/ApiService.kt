package data.networks

import data.models.CityResource
import data.models.CityResponseResource
import data.models.ForecastResponse
import data.models.LocationResource
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

    suspend fun getCities() : List<CityResource>{
        val response = httpClient.get("https://countriesnow.space/api/v0.1/countries/population/cities") {
        }
        var responseBody = response.body<CityResponseResource>()
        return responseBody.data;
    }

    suspend fun getLocation(city: String): LocationResource {
        val response = httpClient.get("https://api.openweathermap.org/geo/1.0/direct?") {
            parameter("q", city)
            parameter("limit", 1)
            parameter("appid", API_KEY)
        }
        var responseBody = response.body<List<LocationResource>>()
        return responseBody[0];
    }

    suspend fun getWeather(location: LatLng): WeatherResource {
        return httpClient.get("https://api.openweathermap.org/data/2.5/weather?") {
            parameter("appid", API_KEY)
            parameter("units", "metric")
            parameter("lon", location.longitude)
            parameter("lat", location.latitude)
        }.body()
    }


    suspend fun getForecast(location: LatLng): ForecastResponse {
        return httpClient.get("https://api.openweathermap.org/data/2.5/forecast?") {
            parameter("appid", API_KEY)
            parameter("units", "metric")
            parameter("lon", location.longitude)
            parameter("lat", location.latitude)
        }.body()
    }

}