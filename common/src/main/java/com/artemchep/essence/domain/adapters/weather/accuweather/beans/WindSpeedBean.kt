package com.artemchep.essence.domain.adapters.weather.accuweather.beans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WindSpeedBean(
    @SerialName("Metric")
    val metric: UnitBean
)
