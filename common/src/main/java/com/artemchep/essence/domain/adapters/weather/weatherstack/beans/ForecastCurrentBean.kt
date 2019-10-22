package com.artemchep.essence.domain.adapters.weather.weatherstack.beans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastCurrentBean(
    @SerialName("wind_speed")
    val wind: Float,
    @SerialName("temperature")
    val temp: Float
)
