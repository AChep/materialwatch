package com.artemchep.essence.domain.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.SparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import arrow.core.Either
import com.artemchep.essence.Cfg
import com.artemchep.essence.domain.live.*
import com.artemchep.essence.domain.models.*
import com.artemchep.essence.domain.ports.WeatherPort
import com.artemchep.essence.domain.viewmodel.base.BaseViewModel

/**
 * @author Artem Chepurnoy
 */
class WatchFaceViewModel(
    application: Application,
    config: Cfg,
    weatherPort: WeatherPort,
    /**
     * The emitter of the time
     */
    val timeLiveData: LiveData<Time>,
    /**
     * The emitter of the ambient mode state
     * data.
     */
    ambientModeLiveData: LiveData<AmbientMode>,
    /**
     * The emitter of the complications
     * data.
     */
    complicationsRawLiveData: LiveData<SparseArray<out (Context, Time) -> Complication>>
) : BaseViewModel(application) {

    val themeLiveData: LiveData<Theme> =
        ThemeLiveData(config, ambientModeLiveData)

    val visibilityLiveData: LiveData<Visibility> =
        VisibilityLiveData(config, ambientModeLiveData)

    val geolocationLiveData: LiveData<Moment<Either<Throwable, Geolocation>>> =
        GeolocationLiveData(context, config, timeLiveData)

    val weatherLiveData: LiveData<Either<Throwable, Weather>> =
        WeatherLiveData(config, weatherPort, ambientModeLiveData, geolocationLiveData)

    val complicationsLiveData: LiveData<Map<Int, Pair<Drawable?, String?>>> =
        ComplicationsLiveData(context, timeLiveData, ambientModeLiveData, complicationsRawLiveData)

    /**
     * @author Artem Chepurnoy
     */
    class Factory(
        private val application: Application,
        /**
         * An instance of the config, used by
         * this application.
         */
        private val config: Cfg,
        /**
         * An implementation of the weather
         * port.
         */
        private val weatherPort: WeatherPort,
        /**
         * The emitter of the time
         */
        private val timeLiveData: LiveData<Time>,
        /**
         * The emitter of the ambient mode state
         * data.
         */
        private val ambientModeLiveData: LiveData<AmbientMode>,
        /**
         * The emitter of the complications
         * data.
         */
        private val complicationsRawLiveData: LiveData<SparseArray<out (Context, Time) -> Complication>>
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return when {
                modelClass.isAssignableFrom(WatchFaceViewModel::class.java) -> {
                    val viewModel = WatchFaceViewModel(
                        application,
                        config,
                        weatherPort,
                        timeLiveData,
                        ambientModeLiveData,
                        complicationsRawLiveData
                    )
                    viewModel as T
                }
                else -> throw IllegalArgumentException()
            }
        }
    }

}