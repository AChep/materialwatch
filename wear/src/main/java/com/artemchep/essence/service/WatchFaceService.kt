package com.artemchep.essence.service

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.wearable.complications.ComplicationData
import android.support.wearable.watchface.CanvasWatchFaceService
import android.support.wearable.watchface.WatchFaceStyle
import android.util.SparseArray
import android.view.SurfaceHolder
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.artemchep.essence.WATCH_COMPLICATIONS
import com.artemchep.essence.domain.models.*
import com.artemchep.essence.extensions.getLongMessage
import com.artemchep.essence.extensions.getShortMessage
import com.artemchep.essence.extensions.isActive
import com.artemchep.essence.ui.drawables.AnalogClockDrawable
import com.artemchep.essence.ui.drawables.installAmbientIn
import com.artemchep.essence.ui.drawables.installCfgIn
import com.artemchep.essence.ui.drawables.installTimeIn
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.coroutines.CoroutineContext

/**
 * @author Artem Chepurnoy
 */
open class WatchFaceService : CanvasWatchFaceService() {

    companion object {
        private const val TAG = "WatchFaceService"
    }

    private val context: Context
        get() = this

    override fun onCreateEngine() = WatchFaceEngine()

    /**
     * @author Artem Chepurnoy
     */
    open inner class WatchFaceEngine : LifecycleAwareWatchFaceEngine() {

        /** Maps complication ids to corresponding complications data */
        private val complicationDataSparse = SparseArray<Complication>()

        private val timeSink = MutableStateFlow(currentTime)

        private val ambientSink = MutableStateFlow(isInAmbientMode)

        // ---- Setup ----

        private val analogClockDrawable = AnalogClockDrawable(this@WatchFaceService)

        override fun onCreate(holder: SurfaceHolder) {
            super.onCreate(holder)
            setActiveComplications(*WATCH_COMPLICATIONS)
            setWatchFaceStyle(
                WatchFaceStyle.Builder(this@WatchFaceService)
                    .setAcceptsTapEvents(true)
                    .build()
            )

            analogClockDrawable.installCfgIn(this, ::invalidate)
            analogClockDrawable.installTimeIn(
                scope = this,
                timeFlow = timeSink,
                ambientFlow = ambientSink,
                invalidate = ::invalidate,
            )
            analogClockDrawable.installAmbientIn(
                scope = this,
                ambientFlow = ambientSink,
                invalidate = ::invalidate,
            )
        }

        override fun onTimeTick() {
            super.onTimeTick()
            timeSink.value = currentTime
        }

        override fun onAmbientModeChanged(inAmbientMode: Boolean) {
            super.onAmbientModeChanged(inAmbientMode)
            ambientSink.value = inAmbientMode
        }

        override fun onComplicationDataUpdate(
            watchFaceComplicationId: Int,
            data: ComplicationData?
        ) {
            super.onComplicationDataUpdate(watchFaceComplicationId, data)
            if (data == null
                || (data.shortText == null && data.shortTitle == null
                        && data.longText == null && data.longTitle == null)
            ) {
                complicationDataSparse.remove(watchFaceComplicationId)
            } else {
                val normalIcon = data.icon?.loadDrawable(context)?.apply {
                    val size = (16 * context.resources.displayMetrics.density).toInt()
                    setBounds(0, 0, size, size)
                    setTint(Color.CYAN)
                }
                val adapter = ComplicationDataAdapter(normalIcon, data)
                    .invoke(
                        this@WatchFaceService,
                        currentTime,
                    )
                complicationDataSparse.put(watchFaceComplicationId, adapter)
            }

            analogClockDrawable.complicationDataSparse = complicationDataSparse.clone()
            invalidate()
        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder?,
            format: Int,
            width: Int,
            height: Int
        ) {
            super.onSurfaceChanged(holder, format, width, height)
            analogClockDrawable.setBounds(0, 0, width, height)
        }

        override fun onDraw(canvas: Canvas, bounds: Rect) {
            super.onDraw(canvas, bounds)
            analogClockDrawable.draw(canvas)
        }

    }

    /**
     * @author Artem Chepurnoy
     */
    open inner class LifecycleAwareWatchFaceEngine : CanvasWatchFaceService.Engine(),
        CoroutineScope,
        LifecycleOwner {

        override val coroutineContext: CoroutineContext
            get() = Dispatchers.Main + job

        private lateinit var job: Job

        @Suppress("LeakingThis")
        private val lifecycleRegistry = LifecycleRegistry(this)

        override fun onCreate(holder: SurfaceHolder) {
            super.onCreate(holder)
            lifecycleRegistry.markState(Lifecycle.State.CREATED)
            job = Job()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                lifecycleRegistry.markState(Lifecycle.State.RESUMED)
            } else {
                lifecycleRegistry.markState(Lifecycle.State.STARTED)
            }
        }

        override fun onDestroy() {
            job.cancel()
            lifecycleRegistry.markState(Lifecycle.State.DESTROYED)
            super.onDestroy()
        }

        override fun getLifecycle(): Lifecycle {
            return lifecycleRegistry
        }

    }

    /**
     * @author Artem Chepurnoy
     */
    private class ComplicationDataAdapter(
        private val normalIcon: Drawable?,
        private val raw: ComplicationData
    ) : (Context, Time) -> Complication {
        override fun invoke(context: Context, time: Time): Complication {
            return Complication(
                normalIconDrawable = normalIcon,
                longMsg = raw.getLongMessage(context, time),
                shortMsg = raw.getShortMessage(context, time),
                isActive = raw.isActive(time)
            )
        }
    }

}