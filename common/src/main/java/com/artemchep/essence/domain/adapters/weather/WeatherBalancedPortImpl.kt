package com.artemchep.essence.domain.adapters.weather

import android.util.Log
import arrow.core.Either
import com.artemchep.essence.domain.exceptions.ApiLimitReachedException
import com.artemchep.essence.domain.models.Geolocation
import com.artemchep.essence.domain.models.Weather
import com.artemchep.essence.domain.ports.WeatherPort
import com.artemchep.essence.ifDebug

/**
 * @author Artem Chepurnoy
 */
class WeatherBalancedPortImpl(providers: List<WeatherPort>) : WeatherPort {

    companion object {
        private const val TAG = "WeatherBalancedPortImpl"
    }

    private val providers = providers.toMutableList()

    override suspend fun getWeather(geolocation: Geolocation): Either<Throwable, Weather> {
        lateinit var e: Throwable
        for (i in 0 until providers.size) {
            val value = providers.first().getWeather(geolocation)
            if (value is Either.Left && value.value is ApiLimitReachedException) {
                e = value.value

                // Resort the providers list, so we won't pull
                // it provider soon.
                providers.removeAt(0).let { providers.add(it) }

                ifDebug {
                    val providersStr = providers
                        .map { it.javaClass.simpleName }
                        .joinToString { it }
                    val msg = "Weather port has thrown an exception; " +
                            "sorted list of providers: $providersStr"
                    Log.w(TAG, msg)
                }
            } else {
                return value
            }
        }

        return Either.Left(e)
    }

}