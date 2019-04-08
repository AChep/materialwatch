package com.artemchep.essence.domain.live

import androidx.lifecycle.LiveData
import com.artemchep.essence.Cfg
import com.artemchep.essence.domain.live.base.BaseLiveData
import com.artemchep.essence.domain.models.AmbientMode
import com.artemchep.essence.domain.models.Theme
import com.artemchep.essence.extensions.launchObserver
import com.artemchep.essence.extensions.produceFromLive
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
    private val ambientModeLiveData: LiveData<AmbientMode>
) : BaseLiveData<Theme>() {

    override fun onActive() {
        super.onActive()
        launchObserver(config) { updateTheme() }
        launch {
            produceFromLive(ambientModeLiveData).consumeEach { updateTheme() }
        }

        updateTheme()
    }

    private fun updateTheme() {
        val inAmbientMode = ambientModeLiveData.value!!.isOn
        val theme = if (inAmbientMode) {
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

        // Post value only if something
        // have changed.
        if (theme != value) {
            postValue(theme)
        }
    }

}
