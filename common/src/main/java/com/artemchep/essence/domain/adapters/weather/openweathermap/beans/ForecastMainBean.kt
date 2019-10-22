package com.artemchep.essence.domain.adapters.weather.openweathermap.beans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastMainBean(
    @SerialName("temp")
    val temp: Float
)
