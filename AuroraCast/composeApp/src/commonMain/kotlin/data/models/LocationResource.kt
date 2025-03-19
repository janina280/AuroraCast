package data.models
import kotlinx.serialization.Serializable

@Serializable
data class LocationResource(
    val name: String?,
    val lat: Double,
    val lon: Double
)