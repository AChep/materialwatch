package com.artemchep.essence.domain.adapters.weather.weatherstack

import arrow.core.Either
import com.artemchep.essence.domain.adapters.json
import com.artemchep.essence.domain.adapters.weather.weatherstack.beans.ForecastBean
import com.artemchep.essence.domain.models.*
import com.artemchep.essence.domain.ports.WeatherPort
import com.artemchep.essence_common.BuildConfig
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.coroutines.awaitObjectResponse
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.serialization.kotlinxDeserializerOf
import kotlinx.coroutines.Dispatchers

private const val API_KEY = BuildConfig.API_WEATHER_STACK

private const val ENDPOINT = "http://api.weatherstack.com"

/**
 * @author Artem Chepurnoy
 */
class WeatherWeatherStackPortImpl : WeatherPort {

    override suspend fun getWeather(geolocation: Geolocation): Either<Throwable, Weather> {
        val forecast = try {
            createRequest(geolocation).await<ForecastBean>()
        } catch (e: Throwable) {
            return Either.Left(e)
        }

        // Convert the bean to the common weather
        // model.
        return Either.Right(
            Weather(
                current = WeatherCurrent(
                    wind = Wind(mps = forecast.current.wind),
                    temp = Temperature(c = forecast.current.temp)
                ),
                today = null
            )
        )
    }

    private fun createRequest(geolocation: Geolocation) =
        "$ENDPOINT/current"
            .httpGet(
                listOf(
                    "access_key" to API_KEY,
                    "query" to "${geolocation.latitude},${geolocation.longitude}",
                    "units" to "m"
                )
            )

    private suspend inline fun <reified T : Any> Request.await() =
        awaitObjectResponse<T>(
            kotlinxDeserializerOf(json),
            Dispatchers.IO
        ).third

}
