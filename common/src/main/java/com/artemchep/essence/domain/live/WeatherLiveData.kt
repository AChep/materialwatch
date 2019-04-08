package com.artemchep.essence.domain.live

import android.util.Log
import androidx.lifecycle.LiveData
import arrow.core.Either
import arrow.core.flatMap
import com.artemchep.essence.Cfg
import com.artemchep.essence.domain.live.base.BaseLiveData
import com.artemchep.essence.domain.models.AmbientMode
import com.artemchep.essence.domain.models.Geolocation
import com.artemchep.essence.domain.models.Moment
import com.artemchep.essence.domain.models.Weather
import com.artemchep.essence.domain.ports.WeatherPort
import com.artemchep.essence.extensions.produceFromLive
import com.artemchep.essence.extensions.receive
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
    private val ambientModeLiveData: LiveData<AmbientMode>,
    /**
     * The emitter of the geolocation
     * data.
     */
    private val geolocationLiveData: LiveData<Moment<Either<Throwable, Geolocation>>>
) : BaseLiveData<Either<Throwable, Weather>>() {

    companion object {
        private const val TAG = "WeatherLiveData"
    }

    private var weatherJob: Job? = null

    override fun onActive() {
        super.onActive()
        launch {
            produceFromLive(ambientModeLiveData).consumeEach {
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
                postValue(value)
            }.apply {
                invokeOnCompletion {
                    weatherJob = null
                }
            }
        } else ifDebug {
            Log.d(TAG, "Ignoring an update job.")
        }
    }

    private suspend fun getWeather(): Either<Throwable, Weather> = coroutineScope {
        withContext(Dispatchers.Main) { geolocationLiveData.receive() }
            .value
            .flatMap {
                weatherPort.getWeather(it)
            }
    }

}
