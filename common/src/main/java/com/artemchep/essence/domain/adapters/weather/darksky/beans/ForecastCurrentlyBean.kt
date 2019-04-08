package com.artemchep.essence.domain.adapters.weather.darksky.beans

import com.artemchep.essence.domain.models.Temperature
import com.artemchep.essence.domain.models.Wind
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class ForecastCurrentlyBean(
    @SerialName("windSpeed")
    val wind: Float,
    @SerialName("temperature")
    val temp: Float
)
