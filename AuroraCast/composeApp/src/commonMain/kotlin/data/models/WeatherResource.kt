package data.models
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResource(
    val main: Main?,
    val name: String?,
    val visibility: Int?,
    val weather: List<Weather>,
    val wind: Wind?
)