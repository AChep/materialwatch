package com.artemchep.essence.domain.models

/**
 * @author Artem Chepurnoy
 */
data class Weather(
    val current: WeatherCurrent?,
    val today: WeatherToday?
)
