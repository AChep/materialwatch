package com.artemchep.essence.domain.adapters.weather.accuweather.beans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TemperatureCurrentBean(
    @SerialName("Metric")
    val metric: UnitBean
)
