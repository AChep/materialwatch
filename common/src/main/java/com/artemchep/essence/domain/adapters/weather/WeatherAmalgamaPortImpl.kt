package com.artemchep.essence.domain.adapters.weather

import arrow.core.Either
import arrow.core.orNull
import arrow.core.right
import com.artemchep.essence.domain.models.Geolocation
import com.artemchep.essence.domain.models.Time
import com.artemchep.essence.domain.models.Weather
import com.artemchep.essence.domain.ports.WeatherPort

/**
 * @author Artem Chepurnoy
 */
class WeatherAmalgamaPortImpl(
    private val currentWeatherProvider: WeatherPort,
    private val dailyWeatherProvider: WeatherPort
) : WeatherPort {

    companion object {
        private const val MIN_TIMEOUT = 1000 * 60 * 60 * 12 // 12h
    }

    private var lastEntry: Entry<Weather>? = null

    override suspend fun getWeather(geolocation: Geolocation): Either<Throwable, Weather> {
        val now = System.currentTimeMillis().let(::Time)

        // Get min/max weather of the
        // day.
        val dailyWeather = lastEntry
            ?.let {
                val dt = now.millis - it.time.millis
                if (dt < MIN_TIMEOUT) {
                    // The elapsed time is not much, the weather is
                    // the same.
                    it.value.right()
                } else {
                    null
                }
            }
        // We should perform a new request to
        // the weather port.
            ?: try {
                return dailyWeatherProvider.getWeather(geolocation)
                    .also {
                        val weather = it.orNull()
                        if (weather != null) {
                            // Save this object
                            val now = System.currentTimeMillis().let(::Time)
                            lastEntry = Entry(weather, now)
                        }
                    }
            } catch (e: Throwable) {
                lastEntry?.value
            }

        return currentWeatherProvider.getWeather(geolocation)
            .map {
                it.copy(today = it.today ?: lastEntry?.value?.today)
            }
    }

    internal data class Entry<T>(val value: T, val time: Time)

}