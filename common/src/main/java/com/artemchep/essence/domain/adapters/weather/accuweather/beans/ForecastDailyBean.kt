package com.artemchep.essence.domain.adapters.weather.accuweather.beans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class ForecastDailyBean(
    @SerialName("DailyForecasts")
    val days: List<ForecastDailyDayBean>
)
