package com.artemchep.essence.domain.adapters.weather.accuweather.beans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastCurrentlyNowBean(
    @SerialName("Wind")
    val wind: WindBean,
    @SerialName("Temperature")
    val temp: TemperatureCurrentBean
)
