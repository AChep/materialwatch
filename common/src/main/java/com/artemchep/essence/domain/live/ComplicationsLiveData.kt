package com.artemchep.essence.domain.live

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.util.forEach
import com.artemchep.essence.WATCH_COMPLICATIONS
import com.artemchep.essence.domain.live.base.BaseLiveData
import com.artemchep.essence.domain.ports.ComplicationsPort
import com.artemchep.essence.domain.ports.EssentialsPort
import com.artemchep.essence.ifDebug

/**
 * @author Artem Chepurnoy
 */
class ComplicationsLiveData(
    private val context: Context,
    private val complicationsPort: ComplicationsPort,
    private val essentialsPort: EssentialsPort
) : BaseLiveData<Map<Int, Pair<Drawable?, String?>>>() {

    companion object {
        private const val TAG = "ComplicationsLiveData"
    }

    override fun onActive() {
        super.onActive()
        consumeEach(essentialsPort.ambientModeBroadcast) { updateComplications() }
        consumeEach(essentialsPort.timeBroadcast) { updateComplications() }

        updateComplications()
    }

    private fun updateComplications() {
        val sparse = complicationsPort.complicationsBroadcast.value
        val ambientMode = essentialsPort.ambientModeBroadcast.value
        val time = essentialsPort.timeBroadcast.value

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
