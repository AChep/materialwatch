package com.artemchep.essence.domain.adapters.weather

import arrow.core.Either
import com.artemchep.essence.domain.models.Geolocation
import com.artemchep.essence.domain.models.Time
import com.artemchep.essence.domain.models.Weather
import com.artemchep.essence.domain.ports.WeatherPort

/**
 * @author Artem Chepurnoy
 */
class WeatherCachedPortImpl(private val provider: WeatherPort) : WeatherPort {

    companion object {
        private const val TAG = "WeatherCachedPortImpl"

        private const val MIN_TIMEOUT = 1000 * 60 * 15 // 15m
        private const val CURRENT_TIMEOUT = 1000 * 60 * 60 // 1h
        private const val TODAY_TIMEOUT = 1000 * 60 * 60 * 32 // 32h
    }

    private var lastEntry: Entry<Weather>? = null

    override suspend fun getWeather(geolocation: Geolocation): Either<Throwable, Weather> {
        return provider.getWeather(geolocation)
        /*
        val now = System.currentTimeMillis().let(::Time)
        return lastEntry
            ?.let {
                val dt = now.millis - it.time.millis
                if (dt < MIN_TIMEOUT) {
                    ifDebug {
                        Log.d(TAG, "Returned cached value: ${dt}ms is lower than min needed")
                    }
                    // The elapsed time is not much, the weather is
                    // the same.
                    it.value
                } else {
                    null
                }
            }
        // We should perform a new request to
        // the weather port.
            ?: try {
                return provider.getWeather(geolocation)
                    .also {
                        ifDebug {
                            Log.d(TAG, "Storing an updated $it")
                        }
                        // Save this object
                        val now = System.currentTimeMillis().let(::Time)
                        lastEntry = Entry(it, now)
                    }
            } catch (e: Throwable) {
                lastEntry
                    ?.let {
                        val dt = now.millis - it.time.millis

                        ifDebug {
                            val msg = "Returned cached value: got an exception, ${dt}ms"
                            Log.d(TAG, msg, e)
                        }

                        if (dt < CURRENT_TIMEOUT) {
                            // The elapsed time is not much, the weather can be
                            // the same.
                            return it.value
                        } else if (dt < TODAY_TIMEOUT) {
                            return it.value.copy(current = null)
                        }
                    }

                ifDebug {
                    val msg = "Thrown exception"
                    Log.d(TAG, msg, e)
                }

                // We can not show relevant data, throw an
                // exception.
                throw e
            }
            */
    }

    internal data class Entry<T>(val value: T, val time: Time)

}