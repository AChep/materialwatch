package com.artemchep.essence.domain.live

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.SparseArray
import androidx.core.util.forEach
import androidx.lifecycle.LiveData
import com.artemchep.essence.WATCH_COMPLICATIONS
import com.artemchep.essence.domain.live.base.BaseLiveData
import com.artemchep.essence.domain.models.AmbientMode
import com.artemchep.essence.domain.models.Complication
import com.artemchep.essence.domain.models.Time
import com.artemchep.essence.extensions.produceFromLive
import com.artemchep.essence.extensions.receive
import com.artemchep.essence.ifDebug
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * @author Artem Chepurnoy
 */
class ComplicationsLiveData(
    private val context: Context,
    /**
     * The emitter of the time
     */
    private val timeLiveData: LiveData<Time>,
    /**
     * The emitter of the ambient mode state
     * data.
     */
    private val ambientModeLiveData: LiveData<AmbientMode>,
    /**
     * The emitter of the complications
     * data.
     */
    private val complicationsRawLiveData: LiveData<SparseArray<out (Context, Time) -> Complication>>
) : BaseLiveData<Map<Int, Pair<Drawable?, String?>>>() {

    companion object {
        private const val TAG = "ComplicationsLiveData"
    }

    override fun onActive() {
        super.onActive()
        launch {
            produceFromLive(complicationsRawLiveData).consumeEach { updateComplications() }
        }
        launch {
            produceFromLive(ambientModeLiveData).consumeEach { updateComplications() }
        }
        launch {
            produceFromLive(timeLiveData).consumeEach { updateComplications() }
        }

        updateComplications()
    }

    private fun updateComplications() {
        val sparse = runBlocking { complicationsRawLiveData.receive() }
        val ambientMode = runBlocking { ambientModeLiveData.receive() }.isOn
        val time = runBlocking { timeLiveData.receive() }

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

                map[watchFaceComplicationId] = icon to text?.toString()
            }
        }

        ifDebug {
            Log.d(TAG, "Posting new complications: $map")
        }

        postValue(map)
    }

}
