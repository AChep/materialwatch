package com.artemchep.essence.domain.ports

import arrow.core.Either
import com.artemchep.essence.domain.models.Geolocation
import com.artemchep.essence.domain.models.Weather

/**
 * @author Artem Chepurnoy
 */
interface WeatherPort {
    suspend fun getWeather(geolocation: Geolocation): Either<Throwable, Weather>
}
