package com.artemchep.essence.domain.adapters.weather.apixu.beans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastForecastDayBean(
    @SerialName("day")
    val main: ForecastForecastDayMainBean
)
