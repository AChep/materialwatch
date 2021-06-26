package com.artemchep.essence.domain.adapters.weather

import arrow.core.Either
import arrow.core.flatten
import com.artemchep.essence.domain.models.Geolocation
import com.artemchep.essence.domain.models.Weather
import com.artemchep.essence.domain.ports.WeatherPort

/**
 * @author Artem Chepurnoy
 */
class WeatherSafePortImpl(private val provider: WeatherPort) : WeatherPort {
    override suspend fun getWeather(geolocation: Geolocation): Either<Throwable, Weather> {
        return Either.catch {
            provider.getWeather(geolocation)
        }
            .flatten()
    }
}