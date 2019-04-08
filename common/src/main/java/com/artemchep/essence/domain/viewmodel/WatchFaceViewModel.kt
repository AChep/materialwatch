package com.artemchep.essence.domain.viewmodel

import android.app.Application
import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import arrow.core.Either
import com.artemchep.essence.Cfg
import com.artemchep.essence.domain.live.*
import com.artemchep.essence.domain.models.*
import com.artemchep.essence.domain.ports.ComplicationsPort
import com.artemchep.essence.domain.ports.EssentialsPort
import com.artemchep.essence.domain.ports.WeatherPort
import com.artemchep.essence.domain.viewmodel.base.BaseViewModel
import com.artemchep.essence.extensions.toLiveData

/**
 * @author Artem Chepurnoy
 */
class WatchFaceViewModel(
    application: Application,
    config: Cfg,
    complicationsPort: ComplicationsPort,
    weatherPort: WeatherPort,
    essentialsPort: EssentialsPort
) : BaseViewModel(application) {

    val themeLiveData: LiveData<Theme> =
        ThemeLiveData(config, essentialsPort)

    val visibilityLiveData: LiveData<Visibility> =
        VisibilityLiveData(config, essentialsPort)

    val geolocationLiveData: LiveData<Moment<Either<Throwable, Geolocation>>> =
        GeolocationLiveData(context, config, essentialsPort)

    val weatherLiveData: LiveData<Either<Throwable, Weather>> =
        WeatherLiveData(config, weatherPort, essentialsPort, geolocationLiveData)

    val complicationsLiveData: LiveData<Map<Int, Pair<Drawable?, String?>>> =
        ComplicationsLiveData(context, complicationsPort, essentialsPort)

    val timeLiveData: LiveData<Time> = essentialsPort.timeBroadcast.toLiveData()

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
         * An implementation of the complications
         * port.
         */
        private val complicationsPort: ComplicationsPort,
        /**
         * An implementation of the weather
         * port.
         */
        private val weatherPort: WeatherPort,
        /**
         * An implementation of the essentials
         * port.
         */
        private val essentialsPort: EssentialsPort
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return when {
                modelClass.isAssignableFrom(WatchFaceViewModel::class.java) -> {
                    val viewModel = WatchFaceViewModel(
                        application,
                        config,
                        complicationsPort,
                        weatherPort,
                        essentialsPort
                    )
                    viewModel as T
                }
                else -> throw IllegalArgumentException()
            }
        }
    }

}