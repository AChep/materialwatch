package com.artemchep.essence.domain.live

import androidx.lifecycle.LiveData
import com.artemchep.essence.Cfg
import com.artemchep.essence.domain.live.base.BaseLiveData
import com.artemchep.essence.domain.models.AmbientMode
import com.artemchep.essence.domain.models.Visibility
import com.artemchep.essence.extensions.launchObserver
import com.artemchep.essence.extensions.produceFromLive
import com.artemchep.essence.extensions.receive
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * @author Artem Chepurnoy
 */
class VisibilityLiveData(
    private val config: Cfg,
    /**
     * The emitter of the ambient mode state
     * data.
     */
    private val ambientModeLiveData: LiveData<AmbientMode>
) : BaseLiveData<Visibility>() {

    override fun onActive() {
        super.onActive()
        launchObserver(config) { updateVisibility() }
        launch {
            produceFromLive(ambientModeLiveData).consumeEach { updateVisibility() }
        }

        updateVisibility()
    }

    private fun updateVisibility() {
        val inAmbientMode = runBlocking { ambientModeLiveData.receive().isOn }
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
