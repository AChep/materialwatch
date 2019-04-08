package com.artemchep.essence.live

import com.artemchep.essence.domain.live.base.BaseLiveData
import com.artemchep.essence.domain.models.AmbientMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * @author Artem Chepurnoy
 */
class AmbientModeLiveData : BaseLiveData<AmbientMode>() {
    companion object {
        private const val AMBIENT_MODE_PERIOD = 3500L
    }

    init {
        value = AmbientMode.Off
    }

    override fun onActive() {
        super.onActive()

        launch {
            var ambientMode: AmbientMode = AmbientMode.On

            while (isActive) {
                ambientMode = ambientMode.toggle()
                    .also {
                        postValue(it)
                    }

                // Wait a few seconds before toggling the
                // ambient mode again.
                delay(AMBIENT_MODE_PERIOD)
            }
        }
    }
}