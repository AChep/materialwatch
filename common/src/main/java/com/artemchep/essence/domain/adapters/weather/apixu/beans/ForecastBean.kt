package com.artemchep.essence.domain.adapters.weather.apixu.beans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastBean(
    @SerialName("current")
    val current: ForecastCurrentBean,
    @SerialName("forecast")
    val forecast: ForecastForecastBean
)
