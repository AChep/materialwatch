package com.artemchep.essence.domain.live

import com.artemchep.essence.Cfg
import com.artemchep.essence.domain.live.base.BaseLiveData
import com.artemchep.essence.domain.models.Visibility
import com.artemchep.essence.domain.ports.EssentialsPort
import com.artemchep.essence.extensions.launchObserver
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

/**
 * @author Artem Chepurnoy
 */
class VisibilityLiveData(
    private val config: Cfg,
    private val essentialsPort: EssentialsPort
) : BaseLiveData<Visibility>() {

    override fun onActive() {
        super.onActive()
        launchObserver(config) { updateVisibility() }
        launch {
            essentialsPort.ambientModeBroadcast.consumeEach { updateVisibility() }
        }

        updateVisibility()
    }

    private fun updateVisibility() {
        val inAmbientMode = essentialsPort.ambientModeBroadcast.value
        val visibility = Visibility(
            isTopStartVisible = !inAmbientMode,
            isTopEndVisible = !inAmbientMode,
            isBottomStartVisible = !inAmbientMode,
            isBottomEndVisible = !inAmbientMode
        )

        // Post value only if something
        // have changed.
        if (visibility != value) {
            postValue(visibility)
        }
    }

}
