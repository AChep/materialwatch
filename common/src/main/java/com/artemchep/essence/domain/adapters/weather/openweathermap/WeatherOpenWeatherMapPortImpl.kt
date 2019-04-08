package com.artemchep.essence.domain.adapters.weather.openweathermap

import arrow.core.Either
import com.artemchep.essence.domain.exceptions.ApiLimitReachedException
import com.artemchep.essence.domain.models.Geolocation
import com.artemchep.essence.domain.models.Weather
import com.artemchep.essence.domain.ports.WeatherPort
import kotlinx.serialization.ImplicitReflectionSerializer

private const val API_KEY = "cb2338d61ea34fe2ba0111915192603"

private const val ENDPOINT = "https://api.apixu.com"

/**
 * @author Artem Chepurnoy
 */
class WeatherOpenWeatherMapPortImpl : WeatherPort {

    @ImplicitReflectionSerializer
    override suspend fun getWeather(geolocation: Geolocation): Either<Throwable, Weather> =
        Either.left(ApiLimitReachedException())

}
