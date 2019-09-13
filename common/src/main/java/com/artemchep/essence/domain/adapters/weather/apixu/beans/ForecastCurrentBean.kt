package com.artemchep.essence.domain.adapters.weather.apixu.beans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastCurrentBean(
    @SerialName("temp_c")
    val temp: Float,
    @SerialName("wind_mph")
    val windMph: Float
)
