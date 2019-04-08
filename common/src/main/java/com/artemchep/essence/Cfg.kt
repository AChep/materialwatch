package com.artemchep.essence

import com.artemchep.config.common.SharedPrefConfig
import com.artemchep.essence.ui.PALETTE_WHITE

/**
 * @author Artem Chepurnoy
 */
object Cfg : SharedPrefConfig("config") {

    const val KEY_ACCENT_COLOR = "accent"
    // Theme
    const val KEY_THEME = "theme"
    const val THEME_BLACK = "BLACK"
    const val THEME_DARK = "DARK"
    const val THEME_LIGHT = "LIGHT"
    // Geolocation
    const val KEY_GEOLOCATION_UPDATE_PERIOD = "geolocation::update_period"
    const val GEOLOCATION_UPDATE_PERIOD = 1000L * 60L * 20L // 20 mins
    // Weather
    const val KEY_WEATHER_UPDATE_PERIOD = "weather::update_period"
    const val WEATHER_UPDATE_PERIOD = 1000L * 60L * 20L // 20 mins

    var accentColor: Int by configDelegate(KEY_ACCENT_COLOR, PALETTE_WHITE)
    var themeName: String by configDelegate(KEY_THEME, THEME_BLACK)

    /**
     * How much time should we wait between updating
     * geolocation.
     */
    var geolocationUpdatePeriod: Long by configDelegate(
        key = KEY_GEOLOCATION_UPDATE_PERIOD,
        defaultValue = GEOLOCATION_UPDATE_PERIOD
    )

    /**
     * How much time should we wait between updating
     * weather.
     */
    var weatherUpdatePeriod: Long by configDelegate(
        key = KEY_WEATHER_UPDATE_PERIOD,
        defaultValue = WEATHER_UPDATE_PERIOD
    )

}