package com.artemchep.essence.ui.views.arc

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.artemchep.essence.ui.views.arc.impl.NaiveArcAlgorithm
import com.artemchep.essence.ui.views.arc.impl.RsArcAlgorithm

/**
 * FrameLayout that blurs its underlying content.
 * Can have children and draw them over blurred background.
 */
class ArcLayout @kotlin.jvm.JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "ArcLayout"
    }

    private var bitmapCache: Bitmap? = null

    private var surface: Surface? = null

    private val algorithm: ArcAlgorithm = if (isInEditMode) {
        NaiveArcAlgorithm()
    } else {
        RsArcAlgorithm(context)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        algorithm.recycle()
        surface?.recycle()
        surface = null
    }

    override fun dispatchDraw(canvas: Canvas) {
        val beginTime = SystemClock.currentThreadTimeMillis()
        val cache = bitmapCache
            ?.takeIf { bitmap ->
                bitmap.width == width &&
                        bitmap.height == height
            }
        val b = cache ?: run {
            // Draw and bend the content
            // of a frame.
            val s = prepareSurface()
            super.dispatchDraw(s.canvas)
            return@run algorithm.bend(s.bitmap)
        }.also {
            bitmapCache = it
        }

        // Draw the resulting
        // bitmap
        canvas.drawBitmap(b, 0.0f, 0.0f, null)

        val endTime = SystemClock.currentThreadTimeMillis()
        val detail = " [from cache]".takeIf { cache != null }.orEmpty()
        val message =
            "Drawing an arc$detail took ${endTime - beginTime}ms."
        Log.i(TAG, message)
    }

    private fun prepareSurface(): Surface {
        return surface
            ?.takeIf { it.width == width && it.height == height }
            ?.apply {
                bitmap.eraseColor(Color.TRANSPARENT)
            }
        // Otherwise create a new bitmap and a new
        // canvas bound to it.
            ?: run {
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444)
                val canvas = Canvas(bitmap)
                return@run Surface(bitmap, canvas)
            }
    }

    fun clearBitmapCache() {
        bitmapCache = null
    }

    private inner class Surface(
        val bitmap: Bitmap,
        val canvas: Canvas
    ) {
        init {
            algorithm.prepare(bitmap)
        }

        val width: Int get() = bitmap.width
        val height: Int get() = bitmap.height

        /**
         * Recycles the bitmap of this
         * surface.
         */
        fun recycle() {
            bitmap.recycle()
        }
    }
}