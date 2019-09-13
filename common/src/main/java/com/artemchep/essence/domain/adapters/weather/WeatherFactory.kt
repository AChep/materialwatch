package com.artemchep.essence.domain.adapters.weather

import com.artemchep.essence.domain.adapters.weather.accuweather.WeatherAccuWeatherPortImpl
import com.artemchep.essence.domain.adapters.weather.apixu.WeatherApixuPortImpl
import com.artemchep.essence.domain.adapters.weather.darksky.WeatherDarkSkyPortImpl
import com.artemchep.essence_common.BuildConfig

fun WeatherPort() =
    listOf(
//        WeatherOpenWeatherMapPortImpl(),
        WeatherDarkSkyPortImpl(),
        WeatherApixuPortImpl(),
        WeatherAccuWeatherPortImpl()
    )
        .map(::WeatherSafePortImpl)
        .run {
            // Shuffle the providers in release mode, so
            // it distributes evenly.
            takeIf { BuildConfig.DEBUG } ?: shuffled()
        }
        .let(::WeatherBalancedPortImpl)
        .let(::WeatherCachedPortImpl)
