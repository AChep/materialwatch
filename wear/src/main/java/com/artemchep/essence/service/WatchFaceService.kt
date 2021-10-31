package com.artemchep.essence.service

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Build
import android.support.wearable.complications.ComplicationData
import android.support.wearable.watchface.CanvasWatchFaceService
import android.support.wearable.watchface.WatchFaceStyle
import android.util.SparseArray
import android.view.SurfaceHolder
import androidx.core.util.forEach
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import arrow.core.memoize
import com.artemchep.essence.Cfg
import com.artemchep.essence.WATCH_COMPLICATIONS
import com.artemchep.essence.domain.models.*
import com.artemchep.essence.extensions.getLongMessage
import com.artemchep.essence.extensions.getShortMessage
import com.artemchep.essence.extensions.isActive
import com.artemchep.essence.ui.drawables.AnalogClockDrawable
import com.artemchep.essence.ui.drawables.installAmbientIn
import com.artemchep.essence.ui.drawables.installCfgIn
import com.artemchep.essence.ui.drawables.installTimeIn
import com.artemchep.mw.R
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.*
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

        private val complicationSink = MutableStateFlow(SparseArray<ComplicationDataBuilder>())

        private fun <T> MutableStateFlow<SparseArray<T>>.updateClone(
            block: SparseArray<T>.() -> Unit,
        ) = update { sparseArray ->
            sparseArray.clone().apply(block)
        }

        private val timeSink = MutableStateFlow(currentTime)

        private val cFlow = combine(
            complicationSink,
            timeSink,
            Cfg.asFlowOfProperty<String>(Cfg.KEY_THEME),
            Cfg.asFlowOfProperty<ComplicationEditor>(Cfg.KEY_COMPLICATION_EDITOR),
        ) { complications, time, themeName, config ->
            val theme = when (themeName) {
                Cfg.THEME_BLACK -> Theme.BLACK
                Cfg.THEME_DARK -> Theme.DARK
                Cfg.THEME_LIGHT -> Theme.LIGHT
                else -> Theme.BLACK
            }

            val sparse = SparseArray<Complication2>()
            complications.forEach { key, value ->
                val item = config.getOrCreate(key)
                    .run {
                        if (iconColor == null) {
                            copy(iconColor = theme.contentColor)
                        } else this
                    }
                sparse.put(
                    key, value.build(
                        config = item,
                        time = time,
                    )
                )
            }
            sparse
        }.flowOn(Dispatchers.Default)

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

            cFlow
                .onEach {
                    analogClockDrawable.complicationDataSparse = it
                    if (!isInAmbientMode)
                        invalidate()
                }
                .flowOn(Dispatchers.Main)
                .launchIn(this)
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
            if (data == null) {
                complicationSink.updateClone {
                    remove(watchFaceComplicationId)
                }
            } else {
                complicationSink.updateClone {
                    get(watchFaceComplicationId)
                        ?.apply {
                            source = data
                            return@updateClone
                        }
                    // otherwise create a new builder
                    val builder = ComplicationDataBuilder(
                        context = context,
                        id = watchFaceComplicationId,
                        source = data,
                    )
                    put(watchFaceComplicationId, builder)
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

private data class ComplicationDataBuilder(
    val context: Context,
    val id: Int,
    var source: ComplicationData,
) {
    private val loadMemoizedIcon = memoizeLast<Icon?, Drawable?> { icon ->
        icon?.loadDrawable()
            ?.apply {
                val size = context.resources.getDimensionPixelSize(R.dimen.watch_face_icon_size)
                setBounds(0, 0, size, size)
            }
    }

    private fun Icon.loadDrawable() = loadDrawable(context)

    fun build(
        config: ComplicationEditor.Item,
        time: Time,
    ): Complication2 {
        val iconEnabled = config.iconEnabled ?: ComplicationEditor.Item.defaultIconEnabled
        val icon = source.icon
            .takeIf { iconEnabled }
            .let(loadMemoizedIcon)
            ?.apply {
                setTint(config.iconColor ?: Color.BLUE)
            }
        return Complication2(
            id = id,
            action = source.tapAction,
            icon = icon,
            text = source.getShortMessage(context, time),
        )
    }
}

private fun <T, R> memoizeLast(
    distinct: (T, T) -> Boolean = { a, b -> a == b },
    block: (T) -> R
): (T) -> R {
    val noneValue = Any()
    var lastValue: Any? = noneValue
    var lastResult: Any? = null
    return { value ->
        if (lastValue === noneValue || !distinct(lastValue as T, value)) {
            lastResult = block(value)
            lastValue = value
        }
        lastResult as R
    }
}
