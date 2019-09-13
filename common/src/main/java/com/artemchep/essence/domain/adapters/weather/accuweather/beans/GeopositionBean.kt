package com.artemchep.essence.domain.adapters.weather.accuweather.beans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeopositionBean(
    @SerialName("Key")
    val key: String
)
