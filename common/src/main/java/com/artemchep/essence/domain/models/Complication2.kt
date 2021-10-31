package com.artemchep.essence.domain.models

import android.app.PendingIntent
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.TextPaint
import kotlin.math.PI

/**
 * @author Artem Chepurnoy
 */
data class Complication2(
    val id: Int,
    val action: PendingIntent? = null,
    val icon: Drawable? = null,
    val text: CharSequence? = null,
)

class ComplicationLayout {
    fun layout(
        list: List<Complication2>,
        radius: Float,
        paint: TextPaint,
    ) {
        // Total length of the arc, in pixels.
        val totalLength = 2f * radius * PI.toFloat()
        list
            .map { complication ->
                val iconLength = complication.icon?.bounds?.width() ?: 0
                val textLength = complication.calculateTextLengthPx(paint)
                1.5f * iconLength + textLength
            }
    }
}

class ComplicationLayoutData(
    val draw: (canvas: Canvas, paint: TextPaint) -> Unit,
)

private val tempBounds = Rect()

/**
 * Calculates the length of the text in pixels.
 * This is a computation-heavy method.
 */
fun Complication2.calculateTextLengthPx(paint: TextPaint): Int {
    val text = text?.toString()
        ?: return 0
    paint.getTextBounds(text, 0, text.length, tempBounds)
    return tempBounds.width()
}
