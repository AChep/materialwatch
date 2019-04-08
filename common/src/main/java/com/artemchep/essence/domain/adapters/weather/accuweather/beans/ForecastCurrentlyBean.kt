package com.artemchep.essence.domain.adapters.weather.accuweather.beans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class ForecastCurrentlyBean(
    @SerialName("Wind")
    val wind: WindBean,
    @SerialName("Temperature")
    val temp: TemperatureCurrentBean
)
