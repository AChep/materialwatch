package com.artemchep.essence.domain.models

import com.artemchep.essence.Cfg

const val SETTINGS_ITEM_UNKNOWN = 1
const val SETTINGS_ITEM_COMPLICATIONS = 1
const val SETTINGS_ITEM_THEME = 2
const val SETTINGS_ITEM_ACCENT = 3
const val SETTINGS_ITEM_ABOUT = 4

fun configKeyToSettingsKey(key: String) = when (key) {
    Cfg.KEY_ACCENT_COLOR -> SETTINGS_ITEM_ACCENT
    Cfg.KEY_THEME -> SETTINGS_ITEM_THEME
    else -> SETTINGS_ITEM_UNKNOWN
}
