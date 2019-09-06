package com.artemchep.essence.domain.live

import android.util.Log
import arrow.core.Either
import arrow.core.flatMap
import arrow.core.orNull
import com.artemchep.essence.Cfg
import com.artemchep.essence.domain.exceptions.GeolocationAccessException
import com.artemchep.essence.domain.exceptions.GeolocationEmptyException
import com.artemchep.essence.domain.live.base.Live3
import com.artemchep.essence.domain.models.AmbientMode
import com.artemchep.essence.domain.models.Geolocation
import com.artemchep.essence.domain.models.Moment
import com.artemchep.essence.domain.models.Weather
import com.artemchep.essence.domain.ports.WeatherPort
import com.artemchep.essence.ifDebug
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach

/**
 * @author Artem Chepurnoy
 */
class WeatherLiveData(
    private val config: Cfg,
    /**
     * The provider of a
     * weather.
     */
    private val weatherPort: WeatherPort,
    /**
     * The emitter of the ambient mode state
     * data.
     */
    private val ambientModeLiveData: Live3<AmbientMode>,
    /**
     * The emitter of the geolocation
     * data.
     */
    private val geolocationLiveData: Live3<Moment<Either<Throwable, Geolocation>>>
) : Live3<Either<Throwable, Weather>>(Either.left(GeolocationEmptyException())) {

    companion object {
        private const val TAG = "WeatherLiveData"
    }

    private var weatherJob: Job? = null

    override fun onActive() {
        super.onActive()
        launch {
            geolocationLiveData.openSubscription(this)
                .consumeEach {
                    updateWeather()
                }
        }
        launch {
            ambientModeLiveData.openSubscription(this)
                .consumeEach {
                    updateWeather()
                }
        }

        launch {
            while (isActive) {
                delay(1000 * 60 * 30)
                updateWeather()
            }
        }

        updateWeather()
    }

    private fun updateWeather() {
        if (weatherJob?.isActive != true) {
            ifDebug {
                Log.d(TAG, "Launching an update job.")
            }

            // Create a new `retrieve current weather` job
            // and remember it.
            weatherJob = launch(Dispatchers.IO) {
                val value = getWeather()
                push(value)

                if (value
                        .mapLeft {
                            it is GeolocationAccessException ||
                                    it is GeolocationEmptyException
                        }
                        .swap()
                        .orNull() == true
                ) {
                    // Schedule a weather update
                    launch {
                        delay(1000L)
                        updateWeather()
                    }
                }
            }.apply {
                invokeOnCompletion {
                    weatherJob = null
                }
            }
        } else ifDebug {
            Log.d(TAG, "Ignoring an update job.")
        }
    }

    private suspend fun getWeather(): Either<Throwable, Weather> =
        geolocationLiveData.value
            .unbox()
            .flatMap {
                weatherPort.getWeather(it)
            }

}
