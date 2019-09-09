package com.artemchep.essence.service

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.wearable.complications.ComplicationData
import android.support.wearable.watchface.CanvasWatchFaceService
import android.support.wearable.watchface.WatchFaceStyle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.artemchep.essence.Cfg
import com.artemchep.essence.R
import com.artemchep.essence.WATCH_COMPLICATIONS
import com.artemchep.essence.domain.adapters.geolocation.GmsGeolocationPort
import com.artemchep.essence.domain.adapters.weather.WeatherPort
import com.artemchep.essence.domain.models.AmbientMode
import com.artemchep.essence.domain.models.Complication
import com.artemchep.essence.domain.models.Time
import com.artemchep.essence.domain.models.asAmbientMode
import com.artemchep.essence.domain.viewmodel.WatchFaceViewModel
import com.artemchep.essence.extensions.getLongMessage
import com.artemchep.essence.extensions.getShortMessage
import com.artemchep.essence.extensions.injectObserver
import com.artemchep.essence.extensions.isActive
import com.artemchep.essence.flow.ManualTimeFlow
import com.artemchep.essence.ui.views.WatchFaceView
import com.artemchep.liveflow.impl.MutableLiveFlowImpl
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext

/**
 * @author Artem Chepurnoy
 */
open class WatchFaceService : CanvasWatchFaceService() {

    companion object {
        private const val TAG = "WatchFaceService"

        private const val COMPLICATION_GATHER_DELAY = 500L
    }

    private val context: Context
        get() = this

    override fun onCreateEngine() = WatchFaceEngine()

    /**
     * @author Artem Chepurnoy
     */
    open inner class WatchFaceEngine : LifecycleAwareWatchFaceEngine() {

        /** Maps complication ids to corresponding complications data */
        private val complicationDataSparse = SparseArray<ComplicationDataAdapter>()

        private val complicationDataMutex = Mutex()

        // ---- Ports ----

        private val timeFlow = ManualTimeFlow()

        private val ambientModeFlow = MutableLiveFlowImpl<AmbientMode>()
            .apply {
                emit(AmbientMode.Off)
            }

        private val complicationsRawFlow =
            MutableLiveFlowImpl<SparseArray<out (Context, Time) -> Complication>>()

        private val geolocationPort by lazy { GmsGeolocationPort(this@WatchFaceService) }

        private val weatherPort = WeatherPort()

        // ---- Setup ----

        /** Surface width */
        private var width = 0
        /** Surface height */
        private var height = 0

        private lateinit var view: WatchFaceView

        private lateinit var viewModel: WatchFaceViewModel

        private var sendComplicationsJob: Job? = null

        override fun onCreate(holder: SurfaceHolder) {
            super.onCreate(holder)
            setActiveComplications(*WATCH_COMPLICATIONS)
            setWatchFaceStyle(
                WatchFaceStyle.Builder(this@WatchFaceService)
                    .setAcceptsTapEvents(true)
                    .build()
            )

            view = LayoutInflater
                .from(context)
                .inflate(R.layout.watch_face, null, false)
                .let { it as WatchFaceView }

            viewModel = WatchFaceViewModel(
                application,
                Cfg,
                weatherPort,
                geolocationPort,
                timeFlow.share(),
                ambientModeFlow.share(),
                complicationsRawFlow.share()
            )
            viewModel.setup()
        }

        private fun WatchFaceViewModel.setup() {
            watchFaceFlow.injectObserver(this@WatchFaceEngine) {
                view.setDelta(it)
                postInvalidate()
            }
        }

        override fun onTimeTick() {
            super.onTimeTick()
            timeFlow.emitCurrentTime()
        }

        override fun onAmbientModeChanged(inAmbientMode: Boolean) {
            super.onAmbientModeChanged(inAmbientMode)
            val ambientMode = inAmbientMode.asAmbientMode()
            ambientModeFlow.emit(ambientMode)
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
                runBlocking {
                    complicationDataMutex.withLock {
                        complicationDataSparse.remove(watchFaceComplicationId)
                    }
                }

                postSendComplications()
            } else {
                launch {
                    val (normalIcon, ambientIcon) = withContext(Dispatchers.IO) {
                        val normalIcon = data.icon?.loadDrawable(context)
                        val ambientIcon = data.burnInProtectionIcon?.loadDrawable(context)
                        return@withContext normalIcon to ambientIcon
                    }

                    val adapter = ComplicationDataAdapter(normalIcon, ambientIcon, data)
                    complicationDataMutex.withLock {
                        complicationDataSparse.put(watchFaceComplicationId, adapter)
                    }

                    postSendComplications()
                }
            }
        }

        private fun postSendComplications() {
            sendComplicationsJob?.cancel()
            sendComplicationsJob = launch {
                delay(COMPLICATION_GATHER_DELAY)

                // Copy current sparse array and send to
                // an adapter.
                val sparse = complicationDataMutex
                    .withLock {
                        complicationDataSparse.clone()
                    }
                complicationsRawFlow.emit(sparse)
            }.apply {
                invokeOnCompletion {
                    sendComplicationsJob = null
                }
            }
        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder?,
            format: Int,
            width: Int,
            height: Int
        ) {
            super.onSurfaceChanged(holder, format, width, height)
            this.width = width
            this.height = height
            performViewLayout()
        }

        /**
         * Calls [view]'s [measure][View.measure] and [layout][View.layout] methods,
         * to position its children in place.
         */
        private fun performViewLayout() {
            val measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
            val measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)

            view.apply {
                measure(measuredWidth, measuredHeight)
                layout(0, 0, this.measuredWidth, this.measuredHeight)
            }
        }

        override fun onDraw(canvas: Canvas, bounds: Rect) {
            super.onDraw(canvas, bounds)
            view.apply {
                if (isLayoutRequested) performViewLayout()
                draw(canvas)
            }
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
        private val ambientIcon: Drawable?,
        private val raw: ComplicationData
    ) : (Context, Time) -> Complication {
        override fun invoke(context: Context, time: Time): Complication {
            return Complication(
                normalIconDrawable = normalIcon,
                ambientIconDrawable = ambientIcon,
                longMsg = raw.getLongMessage(context, time),
                shortMsg = raw.getShortMessage(context, time),
                isActive = raw.isActive(time)
            )
        }
    }

}