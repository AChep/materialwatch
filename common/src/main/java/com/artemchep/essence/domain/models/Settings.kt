package com.artemchep.essence.domain.models

import com.artemchep.essence.Cfg

const val SETTINGS_ITEM_UNKNOWN = 1
const val SETTINGS_ITEM_COMPLICATIONS = 1
const val SETTINGS_ITEM_THEME = 2
const val SETTINGS_ITEM_ACCENT = 3
const val SETTINGS_ITEM_ABOUT = 4
const val SETTINGS_ITEM_DIGITAL_CLOCK = 5
const val SETTINGS_ITEM_ACCENT_TINT_BG = 6

fun configKeyToSettingsKey(key: String) = when (key) {
    Cfg.KEY_DIGITAL_CLOCK_ENABLED -> SETTINGS_ITEM_DIGITAL_CLOCK
    Cfg.KEY_ACCENT_BG_ENABLED -> SETTINGS_ITEM_ACCENT_TINT_BG
    Cfg.KEY_ACCENT_COLOR -> SETTINGS_ITEM_ACCENT
    Cfg.KEY_THEME -> SETTINGS_ITEM_THEME
    else -> SETTINGS_ITEM_UNKNOWN
}
