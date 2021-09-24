package com.artemchep.essence.ui.views

import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View

/**
 * @author Artem Chepurnoy
 */
class CurvedTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    data class Item(
        val icon: Drawable?,
        val text: String,
    )

    data class ItemDrawable(
        val icon: Drawable?,
        val text: String,
    )

    /**
     * The content to be displayed
     * in the arc.
     */
    var content: List<Item> = emptyList()

    var startAngle: Float = 0f

    var sweepAngle: Float = 360f

    private val textPaint = TextPaint()
        .apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }
}

private fun calculateContent(
    radius: Double,
    startAngle: Double,
    sweepAngle: Double,
    textPaint: TextPaint,
    textPadding: Double,
    iconSize: Double,
    content: List<CurvedTextView.Item>,
): List<CurvedTextView.ItemDrawable> {
    val sweepWidth = 2.0 * Math.toRadians(sweepAngle) * radius

    run {
        val rect = Rect()
        content
            .map { item ->
                textPaint.getTextBounds(item.text, 0, item.text.length, rect)
                // Create a list of the physical lengths of all of the
                // items texts.
                rect.width()
            }
    }

    TODO()
}
