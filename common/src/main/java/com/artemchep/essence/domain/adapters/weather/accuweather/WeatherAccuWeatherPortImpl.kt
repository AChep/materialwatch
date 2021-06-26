package com.artemchep.essence.domain.adapters.weather.accuweather

import arrow.core.Either
import com.artemchep.essence.domain.adapters.json
import com.artemchep.essence.domain.adapters.weather.accuweather.beans.ForecastCurrentlyBean
import com.artemchep.essence.domain.adapters.weather.accuweather.beans.ForecastDailyBean
import com.artemchep.essence.domain.adapters.weather.accuweather.beans.GeopositionBean
import com.artemchep.essence.domain.models.*
import com.artemchep.essence.domain.ports.WeatherPort
import com.artemchep.essence_common.BuildConfig
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.coroutines.awaitObjectResponse
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.serialization.kotlinxDeserializerOf
import kotlinx.coroutines.Dispatchers

private const val API_KEY = BuildConfig.API_ACCU_WEATHER

private const val ENDPOINT = "http://dataservice.accuweather.com"

/**
 * @author Artem Chepurnoy
 */
class WeatherAccuWeatherPortImpl : WeatherPort {

    override suspend fun getWeather(geolocation: Geolocation): Either<Throwable, Weather> {
        val (current, today) = try {
            val gbean = createGeopositionRequest(geolocation).await<GeopositionBean>()
            val current = createCurrentlyRequest(gbean).await<ForecastCurrentlyBean>()
            val today = createDailyRequest(gbean).await<ForecastDailyBean>()
                .days
                .first()
            current to today
        } catch (e: Throwable) {
            return Either.Left(e)
        }

        return Either.Right(
            Weather(
                current = WeatherCurrent(
                    wind = Wind(mps = current.wind.speed.metric.value),
                    temp = Temperature(c = current.temp.metric.value)
                ),
                today = WeatherToday(
                    tempMax = Temperature(c = today.temp.max.value),
                    tempMin = Temperature(c = today.temp.min.value)
                )
            )
        )
    }

    private fun createGeopositionRequest(geolocation: Geolocation) =
        "$ENDPOINT/locations/v1/cities/geolocation/search"
            .httpGet(
                listOf(
                    "apikey" to API_KEY,
                    "q" to "${geolocation.latitude},${geolocation.longitude}"
                )
            )

    private fun createDailyRequest(gbean: GeopositionBean) =
        "$ENDPOINT/forecasts/v1/daily/1day/${gbean.key}"
            .httpGet(
                listOf(
                    "apikey" to API_KEY,
                    "metric" to true
                )
            )

    private fun createCurrentlyRequest(gbean: GeopositionBean) =
        "$ENDPOINT/currentconditions/v1/${gbean.key}"
            .httpGet(
                listOf(
                    "apikey" to API_KEY,
                    "details" to true
                )
            )

    private suspend inline fun <reified T : Any> Request.await() =
        awaitObjectResponse<T>(
            kotlinxDeserializerOf(json),
            Dispatchers.IO
        ).third

}
