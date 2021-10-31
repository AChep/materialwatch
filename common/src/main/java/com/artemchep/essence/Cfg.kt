package com.artemchep.essence

import com.artemchep.config.common.SharedPrefConfig
import com.artemchep.config.extensions.asFlowOfProperty
import com.artemchep.essence.domain.models.ComplicationEditor
import com.artemchep.essence.domain.models.ComplicationEditorJsonAdapter
import com.artemchep.essence.ui.PALETTE_MATERIAL_YOU
import com.squareup.moshi.Moshi

/**
 * @author Artem Chepurnoy
 */
object Cfg : SharedPrefConfig("config") {
    val moshi: Moshi = Moshi.Builder()
        .build()

    const val KEY_ACCENT_COLOR = "accent"
    const val KEY_ACCENT_BG_ENABLED = "accent_bg_enabled"
    const val KEY_DIGITAL_CLOCK_ENABLED = "digital_clock_enabled"
    const val KEY_COMPLICATION_EDITOR = "complication_editor"

    // Theme
    const val KEY_THEME = "theme"
    const val THEME_BLACK = "BLACK"
    const val THEME_DARK = "DARK"
    const val THEME_LIGHT = "LIGHT"

    var digitalClockEnabled: Boolean by configDelegate(KEY_DIGITAL_CLOCK_ENABLED, true)

    var accentBgEnabled: Boolean by configDelegate(KEY_ACCENT_BG_ENABLED, false)

    var accentColor: Int by configDelegate(KEY_ACCENT_COLOR, PALETTE_MATERIAL_YOU)

    var complicationEditor: ComplicationEditor by configDelegate(
        key = KEY_COMPLICATION_EDITOR,
        defaultValue = ComplicationEditor(),
    )

    var themeName: String by configDelegate(KEY_THEME, THEME_BLACK)

    fun <T : Any> asFlowOfProperty(key: String) =
        asFlowOfProperty(
            delegate = properties
                .first { it.key == key } as ConfigDelegate<T>
        )
}
