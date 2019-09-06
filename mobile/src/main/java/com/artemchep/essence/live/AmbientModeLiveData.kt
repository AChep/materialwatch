package com.artemchep.essence.live

import com.artemchep.essence.domain.live.base.Live3
import com.artemchep.essence.domain.models.AmbientMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * @author Artem Chepurnoy
 */
class AmbientModeLiveData : Live3<AmbientMode>(AmbientMode.On) {
    companion object {
        private const val AMBIENT_MODE_PERIOD = 3500L
    }

    override fun onActive() {
        super.onActive()

        launch {
            var ambientMode: AmbientMode = value!!

            while (isActive) {
                ambientMode = ambientMode.toggle()
                    .also {
                        push(it)
                    }

                // Wait a few seconds before toggling the
                // ambient mode again.
                delay(AMBIENT_MODE_PERIOD)
            }
        }
    }
}