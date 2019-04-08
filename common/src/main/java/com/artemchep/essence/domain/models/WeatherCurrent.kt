package com.artemchep.essence.domain.models

/**
 * @author Artem Chepurnoy
 */
data class WeatherCurrent(
    /** Current wind */
    val wind: Wind,
    /** Current temperature */
    val temp: Temperature
)
