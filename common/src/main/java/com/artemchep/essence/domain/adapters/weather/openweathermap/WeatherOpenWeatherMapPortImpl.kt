package com.artemchep.essence.domain.adapters.weather.openweathermap

import arrow.core.Either
import com.artemchep.essence.domain.adapters.weather.openweathermap.beans.ForecastBean
import com.artemchep.essence.domain.models.*
import com.artemchep.essence.domain.ports.WeatherPort
import com.artemchep.essence_common.BuildConfig
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.coroutines.awaitObjectResponse
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.serialization.kotlinxDeserializerOf
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json

private const val API_KEY = BuildConfig.API_OPEN_WEATHER_MAP

private const val ENDPOINT = "https://api.openweathermap.org"

/**
 * @author Artem Chepurnoy
 */
class WeatherOpenWeatherMapPortImpl : WeatherPort {

    @ImplicitReflectionSerializer
    override suspend fun getWeather(geolocation: Geolocation): Either<Throwable, Weather> {
        val forecast = try {
            createRequest(geolocation).await<ForecastBean>()
        } catch (e: Throwable) {
            return Either.left(e)
        }

        // Convert the bean to the common weather
        // model.
        return Either.right(
            Weather(
                current = WeatherCurrent(
                    wind = Wind(mps = forecast.wind.speed),
                    temp = Temperature(c = forecast.main.temp)
                ),
                today = null
            )
        )
    }

    private fun createRequest(geolocation: Geolocation) =
        "$ENDPOINT/data/2.5/weather"
            .httpGet(
                listOf(
                    "appid" to API_KEY,
                    "lat" to geolocation.latitude,
                    "lon" to geolocation.longitude,
                    "units" to "metric"
                )
            )

    @ImplicitReflectionSerializer
    private suspend inline fun <reified T : Any> Request.await() =
        awaitObjectResponse<T>(
            kotlinxDeserializerOf(Json.nonstrict),
            Dispatchers.IO
        ).third

}
