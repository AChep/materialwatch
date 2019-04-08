package com.artemchep.essence.domain.models

/**
 * @author Artem Chepurnoy
 */
data class WeatherToday(
    /** Minimum temperature today */
    val tempMin: Temperature,
    /** Maximum temperature today */
    val tempMax: Temperature
)
