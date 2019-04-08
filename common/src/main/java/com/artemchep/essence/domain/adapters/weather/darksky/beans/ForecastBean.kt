package com.artemchep.essence.domain.adapters.weather.darksky.beans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class ForecastBean(
    @SerialName("currently")
    val currently: ForecastCurrentlyBean,
    @SerialName("daily")
    val daily: ForecastDailyBean
)
