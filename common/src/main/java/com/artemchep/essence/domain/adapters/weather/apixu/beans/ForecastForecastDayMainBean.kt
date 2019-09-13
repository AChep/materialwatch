package com.artemchep.essence.domain.adapters.weather.apixu.beans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastForecastDayMainBean(
    @SerialName("maxtemp_c")
    val tempMax: Float,
    @SerialName("mintemp_c")
    val tempMin: Float
)
