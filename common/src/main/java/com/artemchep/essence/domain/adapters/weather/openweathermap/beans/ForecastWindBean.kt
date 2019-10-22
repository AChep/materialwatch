package com.artemchep.essence.domain.adapters.weather.openweathermap.beans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastWindBean(
    @SerialName("speed")
    val speed: Float,
    @SerialName("deg")
    val degree: Float
)
