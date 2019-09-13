package com.artemchep.essence.domain.adapters.weather.darksky.beans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastCurrentlyBean(
    @SerialName("windSpeed")
    val wind: Float,
    @SerialName("temperature")
    val temp: Float
)
