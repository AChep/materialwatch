package com.artemchep.essence

import com.artemchep.config.common.SharedPrefConfig
import com.artemchep.config.extensions.asFlowOfProperty
import com.artemchep.essence.ui.PALETTE_MATERIAL_YOU
import com.artemchep.essence.ui.PALETTE_WHITE

/**
 * @author Artem Chepurnoy
 */
object Cfg : SharedPrefConfig("config") {

    const val KEY_ACCENT_COLOR = "accent"
    const val KEY_ACCENT_BG_ENABLED = "accent_bg_enabled"
    const val KEY_DIGITAL_CLOCK_ENABLED = "digital_clock_enabled"
    // Theme
    const val KEY_THEME = "theme"
    const val THEME_BLACK = "BLACK"
    const val THEME_DARK = "DARK"
    const val THEME_LIGHT = "LIGHT"

    var digitalClockEnabled: Boolean by configDelegate(KEY_DIGITAL_CLOCK_ENABLED, true)

    var accentBgEnabled: Boolean by configDelegate(KEY_ACCENT_BG_ENABLED, false)

    var accentColor: Int by configDelegate(KEY_ACCENT_COLOR, PALETTE_MATERIAL_YOU)

    var themeName: String by configDelegate(KEY_THEME, THEME_BLACK)

    fun <T : Any> asFlowOfProperty(key: String) =
        asFlowOfProperty(
            delegate = properties
                .first { it.key == key } as ConfigDelegate<T>
        )
}
