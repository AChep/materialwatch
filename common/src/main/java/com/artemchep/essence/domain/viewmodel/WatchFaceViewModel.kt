package com.artemchep.essence.domain.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.SparseArray
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.artemchep.essence.Cfg
import com.artemchep.essence.domain.flow.*
import com.artemchep.essence.domain.models.*
import com.artemchep.essence.domain.ports.GeolocationPort
import com.artemchep.essence.domain.ports.WeatherPort
import com.artemchep.essence.domain.viewmodel.base.BaseViewModel
import com.artemchep.liveflow.impl.shared
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

/**
 * @author Artem Chepurnoy
 */
class WatchFaceViewModel(
    application: Application,
    config: Cfg,
    weatherPort: WeatherPort,
    geolocationPort: GeolocationPort,
    val timeFlow: Flow<Time>,
    val ambientModeFlow: Flow<AmbientMode>,
    val complicationsRawFlow: Flow<SparseArray<out (Context, Time) -> Complication>>
) : BaseViewModel(application) {

    val themeFlow: Flow<Theme> = ThemeFlow(
        themeNameFlow = config.asFlowOfProperty(Cfg.KEY_THEME),
        accentColorFlow = config.asFlowOfProperty(Cfg.KEY_ACCENT_COLOR),
        ambientModeFlow = ambientModeFlow
    ).shared()

    val visibilityFlow: Flow<Visibility> = VisibilityFlow(
        ambientModeFlow = ambientModeFlow
    ).shared()

    val geolocationFlow: Flow<Moment<Either<Throwable, Geolocation>>> = geolocationPort
        .asFlow(
            permissionsChangedFlow = context.flowOfPermissionChangedEvent(),
            periodFlow = config.asFlowOfProperty(Cfg.KEY_GEOLOCATION_UPDATE_PERIOD),
            timeFlow = timeFlow
        )
        .shared()

    val weatherFlow: Flow<Moment<Either<Throwable, Weather>>> = weatherPort
        .asFlow(
            geolocationFlow = geolocationFlow
                .map { it.unbox() },
            periodFlow = config.asFlowOfProperty(Cfg.KEY_WEATHER_UPDATE_PERIOD),
            timeFlow = timeFlow
        )
        .shared()

    val complicationsFlow: Flow<Map<Int, Pair<Drawable?, String?>>> = ComplicationFlow(
        context = context,
        ambientModeFlow = ambientModeFlow,
        timeFlow = timeFlow,
        complicationsFactoryFlow = complicationsRawFlow
    ).shared()

    val watchFaceFlow: Flow<List<WatchFaceDelta<*>>> = WatchFaceFlow(
        timeFlow = timeFlow,
        themeFlow = themeFlow,
        visibilityFlow = visibilityFlow,
        weatherFlow = weatherFlow
            .map { it.unbox() },
        complicationFlow = complicationsFlow
    )
        .debounce(16)
        .flowOn(Dispatchers.Default)
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(), 1)
        .delta()

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
        private val geolocationPort: GeolocationPort,
        private val timeFlow: Flow<Time>,
        private val ambientModeFlow: Flow<AmbientMode>,
        private val complicationsRawFlow: Flow<SparseArray<out (Context, Time) -> Complication>>
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return when {
                modelClass.isAssignableFrom(WatchFaceViewModel::class.java) -> {
                    val viewModel = WatchFaceViewModel(
                        application,
                        config,
                        weatherPort,
                        geolocationPort,
                        timeFlow,
                        ambientModeFlow,
                        complicationsRawFlow
                    )
                    viewModel as T
                }
                else -> throw IllegalArgumentException()
            }
        }
    }

}