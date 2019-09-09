package com.artemchep.essence.domain.flow

import com.artemchep.essence.Cfg
import com.artemchep.essence.domain.models.AmbientMode
import com.artemchep.essence.domain.models.Theme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@UseExperimental(ExperimentalCoroutinesApi::class)
fun ThemeFlow(
    themeNameFlow: Flow<String>,
    accentColorFlow: Flow<Int>,
    ambientModeFlow: Flow<AmbientMode>
) = themeNameFlow
    .combine(accentColorFlow) { themeName, accentColor ->
        when (themeName) {
            Cfg.THEME_BLACK -> Theme.BLACK
            Cfg.THEME_DARK -> Theme.DARK
            Cfg.THEME_LIGHT -> Theme.LIGHT
            // Fall back to a default black
            // theme.
            else -> Theme.BLACK
        }.copy(clockHourColor = accentColor)
    }
    .combine(ambientModeFlow) { theme, ambientMode ->
        val inAmbientMode = ambientMode.isOn
        return@combine if (inAmbientMode) {
            Theme.AMBIENT
        } else {
            theme
        }
    }
