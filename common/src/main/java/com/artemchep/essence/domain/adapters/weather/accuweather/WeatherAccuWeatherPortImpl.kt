package com.artemchep.essence.domain.adapters.weather.accuweather

import arrow.core.Either
import com.artemchep.essence.domain.adapters.weather.accuweather.beans.ForecastDailyBean
import com.artemchep.essence.domain.adapters.weather.accuweather.beans.GeopositionBean
import com.artemchep.essence.domain.models.Geolocation
import com.artemchep.essence.domain.models.Temperature
import com.artemchep.essence.domain.models.Weather
import com.artemchep.essence.domain.models.WeatherToday
import com.artemchep.essence.domain.ports.WeatherPort
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.coroutines.awaitObjectResponse
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.serialization.kotlinxDeserializerOf
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json

private const val API_KEY = "2dAJbAyqss3KwcneCxzwGMvn4iETLzTj"

private const val ENDPOINT = "http://dataservice.accuweather.com"

/**
 * @author Artem Chepurnoy
 */
class WeatherAccuWeatherPortImpl : WeatherPort {

    @ImplicitReflectionSerializer
    override suspend fun getWeather(geolocation: Geolocation): Either<Throwable, Weather> {
        val gbean = createGeopositionRequest(geolocation).await<GeopositionBean>()
//        val current = createCurrentlyRequest(geolocation).awa.await<ForecastCurrentlyBean>()
        val daily = createDailyRequest(gbean).await<ForecastDailyBean>()

        val today = daily.days.first()
        return Either.right(
            Weather(
                current = null,
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

    @ImplicitReflectionSerializer
    private suspend inline fun <reified T : Any> Request.await() =
        awaitObjectResponse<T>(
            kotlinxDeserializerOf(Json.nonstrict),
            Dispatchers.IO
        ).third

}
