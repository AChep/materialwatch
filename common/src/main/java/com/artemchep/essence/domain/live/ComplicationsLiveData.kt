package com.artemchep.essence.domain.live

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.SparseArray
import androidx.core.util.forEach
import com.artemchep.essence.WATCH_COMPLICATIONS
import com.artemchep.essence.domain.DEFAULT_DEBOUNCE
import com.artemchep.essence.domain.live.base.Live3
import com.artemchep.essence.domain.models.AmbientMode
import com.artemchep.essence.domain.models.Complication
import com.artemchep.essence.domain.models.Time
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author Artem Chepurnoy
 */
class ComplicationsLiveData(
    private val context: Context,
    /**
     * The emitter of the time
     */
    private val timeLiveData: Live3<Time>,
    /**
     * The emitter of the ambient mode state
     * data.
     */
    private val ambientModeLiveData: Live3<AmbientMode>,
    /**
     * The emitter of the complications
     * data.
     */
    private val complicationsRawLiveData: Live3<SparseArray<out (Context, Time) -> Complication>>
) : Live3<Map<Int, Pair<Drawable?, String?>>>(HashMap()) {

    companion object {
        private const val TAG = "ComplicationsLiveData"
    }

    private var complicationsJob: Job? = null

    override fun onActive() {
        super.onActive()
        launch {
            complicationsRawLiveData.openSubscription(this)
                .consumeEach {
                    updateComplications()
                }
        }
        launch {
            ambientModeLiveData.openSubscription(this)
                .consumeEach {
                    updateComplications()
                }
        }
        launch {
            timeLiveData.openSubscription(this)
                .consumeEach {
                    updateComplications()
                }
        }

        updateComplications()
    }

    private fun updateComplications() {
        complicationsJob?.cancel()
        complicationsJob = launch {
            delay(DEFAULT_DEBOUNCE)

            val geolocation = getComplications()
            push(geolocation)
        }.apply {
            invokeOnCompletion {
                complicationsJob = null
            }
        }
    }

    private fun getComplications(): Map<Int, Pair<Drawable?, String?>> {
        val sparse = complicationsRawLiveData.value
        val ambientMode = ambientModeLiveData.value.isOn
        val time = timeLiveData.value

        // Form a map of new complications for current
        // conditions.
        val map = HashMap<Int, Pair<Drawable?, String?>>()
        sparse.forEach { watchFaceComplicationId, complication ->
            if (watchFaceComplicationId !in WATCH_COMPLICATIONS) {
                return@forEach
            }

            val model = complication.invoke(context, time)
            if (model.isActive) {
                val text = model.longMsg ?: model.shortMsg ?: return@forEach // skip if null
                val icon = model.ambientIconDrawable
                    ?.takeIf {
                        ambientMode
                    }
                    ?: model.normalIconDrawable

                map[watchFaceComplicationId] = icon to text.toString()
            }
        }

        return map
    }

}
