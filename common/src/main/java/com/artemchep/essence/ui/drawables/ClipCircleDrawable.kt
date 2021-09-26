package com.artemchep.essence.ui.drawables

import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableWrapper
import androidx.core.graphics.withClip
import java.lang.Integer.min

/**
 * @author Artem Chepurnoy
 */
class ClipCircleDrawable(drawable: Drawable) : DrawableWrapper(drawable) {
    private val path = Path()

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        path.reset()
        path.addCircle(
            bounds.exactCenterX(),
            bounds.exactCenterY(),
            min(bounds.width(), bounds.height()) / 2f,
            Path.Direction.CW
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun draw(canvas: Canvas) {
        canvas.withClip(path) {
            super.draw(canvas)
        }
    }
}