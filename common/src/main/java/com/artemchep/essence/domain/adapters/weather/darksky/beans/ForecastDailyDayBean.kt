package com.artemchep.essence.domain.adapters.weather.darksky.beans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class ForecastDailyDayBean(
    @SerialName("temperatureMin")
    val tempMin: Float,
    @SerialName("temperatureMax")
    val tempMax: Float
)
