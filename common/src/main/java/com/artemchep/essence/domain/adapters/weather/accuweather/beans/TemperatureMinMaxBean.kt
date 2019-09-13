package com.artemchep.essence.domain.adapters.weather.accuweather.beans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TemperatureMinMaxBean(
    @SerialName("Minimum")
    val min: UnitBean,
    @SerialName("Maximum")
    val max: UnitBean
)
