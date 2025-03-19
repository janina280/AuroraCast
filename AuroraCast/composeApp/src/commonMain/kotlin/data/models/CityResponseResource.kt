package data.models

import kotlinx.serialization.Serializable

@Serializable
data class CityResponseResource(
    val msg: String?,
    val data: List<CityResource>,
)

@Serializable
data class CityResource(
    val city: String,
)