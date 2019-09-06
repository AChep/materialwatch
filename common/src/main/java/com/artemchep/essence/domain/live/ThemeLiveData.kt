package com.artemchep.essence.domain.live

import com.artemchep.essence.Cfg
import com.artemchep.essence.domain.live.base.Live3
import com.artemchep.essence.domain.models.AmbientMode
import com.artemchep.essence.domain.models.Theme
import com.artemchep.essence.extensions.launchObserver
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

/**
 * @author Artem Chepurnoy
 */
class ThemeLiveData(
    private val config: Cfg,
    /**
     * The emitter of the ambient mode state
     * data.
     */
    private val ambientModeLiveData: Live3<AmbientMode>
) : Live3<Theme>(Theme.BLACK) {

    private var themeJob: Job? = null

    override fun onActive() {
        super.onActive()
        launchObserver(config) { updateTheme() }
        launch {
            ambientModeLiveData.openSubscription(this)
                .consumeEach {
                    updateTheme()
                }
        }

        updateTheme()
    }

    private fun updateTheme() {
        pushWithDebounce(this, factory = { getTheme() })
    }

    private fun getTheme(): Theme {
        val inAmbientMode = ambientModeLiveData.value.isOn
        return if (inAmbientMode) {
            Theme.AMBIENT
        } else {
            when (config.themeName) {
                Cfg.THEME_BLACK -> Theme.BLACK
                Cfg.THEME_DARK -> Theme.DARK
                Cfg.THEME_LIGHT -> Theme.LIGHT
                // Fall back to a default black
                // theme.
                else -> Theme.BLACK
            }.copy(clockHourColor = config.accentColor)
        }
    }

}
