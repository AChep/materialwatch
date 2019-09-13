package com.artemchep.essence.domain.adapters.weather.darksky.beans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastDailyBean(
    @SerialName("data")
    val days: List<ForecastDailyDayBean>
)
