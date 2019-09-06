package com.artemchep.essence.domain.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.SparseArray
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import arrow.core.Either
import com.artemchep.essence.Cfg
import com.artemchep.essence.domain.live.*
import com.artemchep.essence.domain.live.base.Live3
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
    val timeLiveData: Live3<Time>,
    /**
     * The emitter of the ambient mode state
     * data.
     */
    ambientModeLiveData: Live3<AmbientMode>,
    /**
     * The emitter of the complications
     * data.
     */
    complicationsRawLiveData: Live3<SparseArray<out (Context, Time) -> Complication>>
) : BaseViewModel(application) {

    val themeLiveData: Live3<Theme> =
        ThemeLiveData(config, ambientModeLiveData)

    val visibilityLiveData: Live3<Visibility> =
        VisibilityLiveData(config, ambientModeLiveData)

    val geolocationLiveData: Live3<Moment<Either<Throwable, Geolocation>>> =
        GeolocationLiveData(context, config, timeLiveData)

    val weatherLiveData: Live3<Either<Throwable, Weather>> =
        WeatherLiveData(config, weatherPort, ambientModeLiveData, geolocationLiveData)

    val complicationsLiveData: Live3<Map<Int, Pair<Drawable?, String?>>> =
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
        private val timeLiveData: Live3<Time>,
        /**
         * The emitter of the ambient mode state
         * data.
         */
        private val ambientModeLiveData: Live3<AmbientMode>,
        /**
         * The emitter of the complications
         * data.
         */
        private val complicationsRawLiveData: Live3<SparseArray<out (Context, Time) -> Complication>>
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