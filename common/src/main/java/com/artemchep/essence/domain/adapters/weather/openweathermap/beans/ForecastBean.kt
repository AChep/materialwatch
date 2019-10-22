package com.artemchep.essence.domain.adapters.weather.openweathermap.beans

import com.artemchep.essence.domain.adapters.weather.weatherstack.beans.ForecastCurrentBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastBean(
    @SerialName("main")
    val main: ForecastCurrentBean,
    @SerialName("wind")
    val wind: ForecastWindBean
)
