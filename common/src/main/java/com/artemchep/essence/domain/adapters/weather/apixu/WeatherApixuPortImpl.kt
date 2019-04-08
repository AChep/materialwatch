package com.artemchep.essence.domain.adapters.weather.apixu

import arrow.core.Either
import com.artemchep.essence.domain.adapters.weather.apixu.beans.ForecastBean
import com.artemchep.essence.domain.models.*
import com.artemchep.essence.domain.ports.WeatherPort
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.coroutines.awaitObjectResponse
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.serialization.kotlinxDeserializerOf
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json

private const val API_KEY = "cb2338d61ea34fe2ba0111915192603"

private const val ENDPOINT = "https://api.apixu.com"

/**
 * @author Artem Chepurnoy
 */
class WeatherApixuPortImpl : WeatherPort {

    @ImplicitReflectionSerializer
    override suspend fun getWeather(geolocation: Geolocation): Either<Throwable, Weather> {
        val forecast = try {
            createForecastRequest(geolocation).await<ForecastBean>()
        } catch (e: Throwable) {
            return Either.left(e)
        }

        // Convert the bean to the common weather
        // model.
        val current = forecast.current
        val today = forecast.forecast.days.first()
        return Either.right(
            Weather(
                current = WeatherCurrent(
                    wind = Wind(mps = current.windMph),
                    temp = Temperature(c = current.temp)
                ),
                today = WeatherToday(
                    tempMax = Temperature(c = today.main.tempMax),
                    tempMin = Temperature(c = today.main.tempMin)
                )
            )
        )
    }

    private fun createForecastRequest(geolocation: Geolocation) =
        "$ENDPOINT/v1/forecast.json"
            .httpGet(
                listOf(
                    "key" to API_KEY,
                    "q" to "${geolocation.latitude},${geolocation.longitude}",
                    "days" to 1
                )
            )

    @ImplicitReflectionSerializer
    private suspend inline fun <reified T : Any> Request.await() =
        awaitObjectResponse<T>(
            kotlinxDeserializerOf(Json.nonstrict),
            Dispatchers.IO
        ).third

}
