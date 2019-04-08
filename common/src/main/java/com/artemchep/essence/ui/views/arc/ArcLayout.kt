package com.artemchep.essence.ui.views.arc

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
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
        val s = prepareSurface()
        super.dispatchDraw(s.canvas)
        val b = algorithm.bend(s.bitmap)

        // Draw the resulting
        // bitmap
        canvas.drawBitmap(b, 0.0f, 0.0f, null)
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