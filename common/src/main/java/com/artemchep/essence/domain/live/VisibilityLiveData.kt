package com.artemchep.essence.domain.live

import com.artemchep.essence.Cfg
import com.artemchep.essence.domain.DEFAULT_DEBOUNCE
import com.artemchep.essence.domain.live.base.Live3
import com.artemchep.essence.domain.models.AmbientMode
import com.artemchep.essence.domain.models.Visibility
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author Artem Chepurnoy
 */
class VisibilityLiveData(
    private val config: Cfg,
    /**
     * The emitter of the ambient mode state
     * data.
     */
    private val ambientModeLiveData: Live3<AmbientMode>
) : Live3<Visibility>(Visibility()) {

    private var visibilityJob: Job? = null

    override fun onActive() {
        super.onActive()
        launch {
            ambientModeLiveData.openSubscription(this)
                .consumeEach {
                    updateVisibility()
                }
        }

        updateVisibility()
    }

    private fun updateVisibility() {
        visibilityJob?.cancel()
        visibilityJob = launch {
            delay(DEFAULT_DEBOUNCE)

            val theme = getVisibility()
            if (theme != value) {
                push(theme)
            }
        }.apply {
            invokeOnCompletion {
                visibilityJob = null
            }
        }
    }

    private fun getVisibility(): Visibility {
        val inAmbientMode = ambientModeLiveData.value.isOn
        return Visibility(
            isTopStartVisible = !inAmbientMode,
            isTopEndVisible = !inAmbientMode,
            isBottomStartVisible = !inAmbientMode,
            isBottomEndVisible = !inAmbientMode
        )
    }

}
