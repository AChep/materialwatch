package com.artemchep.essence.domain.adapters.weather.accuweather.beans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastDailyBean(
    @SerialName("DailyForecasts")
    val days: List<ForecastDailyDayBean>
)
