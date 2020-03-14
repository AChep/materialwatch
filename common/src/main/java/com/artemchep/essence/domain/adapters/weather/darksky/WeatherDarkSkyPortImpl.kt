package com.artemchep.essence.domain.adapters.weather.darksky

import arrow.core.Either
import com.artemchep.essence.domain.adapters.weather.darksky.beans.ForecastBean
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

private const val API_KEY = BuildConfig.API_DARK_SKY

/**
 * @author Artem Chepurnoy
 */
class WeatherDarkSkyPortImpl : WeatherPort {

    @ImplicitReflectionSerializer
    override suspend fun getWeather(geolocation: Geolocation): Either<Throwable, Weather> {
        val forecast = try {
            createRequest(geolocation).await<ForecastBean>()
        } catch (e: Throwable) {
            return Either.left(e)
        }

        // Convert the bean to the common weather
        // model.
        val current = forecast.currently
        val today = forecast.daily.days.first()
        return Either.right(
            Weather(
                current = WeatherCurrent(
                    wind = Wind(mps = current.wind),
                    temp = Temperature(c = current.temp)
                ),
                today = WeatherToday(
                    tempMax = Temperature(c = today.tempMax),
                    tempMin = Temperature(c = today.tempMin)
                )
            )
        )
    }

    private fun createRequest(geolocation: Geolocation) =
        "https://api.darksky.net/forecast/$API_KEY/${geolocation.latitude},${geolocation.longitude}"
            .httpGet(
                listOf(
                    "units" to "si"
                )
            )

    @ImplicitReflectionSerializer
    private suspend inline fun <reified T : Any> Request.await() =
        awaitObjectResponse<T>(
            kotlinxDeserializerOf(Json.nonstrict),
            Dispatchers.IO
        ).third

}
