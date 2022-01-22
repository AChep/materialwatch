package com.artemchep.essence.ui.drawables

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.text.format.DateFormat.is24HourFormat
import android.util.SparseArray
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.withRotation
import androidx.core.graphics.withTranslation
import com.artemchep.essence.*
import com.artemchep.essence.domain.models.Complication2
import com.artemchep.essence.domain.models.Theme
import com.artemchep.essence.domain.models.Time
import com.artemchep.essence.ui.util.blend
import com.artemchep.essence.ui.util.blendColor
import com.artemchep.mw_common.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * @author Artem Chepurnoy
 */
class AnalogClockDrawable(
    val context: Context,
) : Drawable() {
    companion object {
        private const val HAND_MINUTE_MIN_FACTOR = 0.75f
        private const val HAND_MINUTE_MAX_FACTOR = 1f
        private const val HAND_HOUR_MIN_FACTOR = 0.42f
        private const val HAND_HOUR_MAX_FACTOR = 0.60f
    }

    private val hasRoundScreen = context.resources.configuration.isScreenRound

    var complicationDataSparse: SparseArray<Complication2>? = null

    var hours: String = "00"
    var minutes: String = "00"

    var timeEnabled: Boolean = true

    var backgroundTintEnabled: Boolean = false

    var backgroundColor: Int = Color.BLACK

    val backgroundAmbientColor: Int = Color.BLACK

    var contentColor: Int = Color.WHITE

    var accentColor: Int = Color.BLUE

    /** The rotation of the hour hand */
    var ambience: Float = 1f
        set(value) {
            field = value
            invalidateSelf()
        }

    /** The rotation of the hour hand */
    var hourHandRotation: Float = 0f
        set(value) {
            field = value
            invalidateSelf()
        }

    /** The rotation of the minute hand */
    var minuteHandRotation: Float = 0f
        set(value) {
            field = value
            invalidateSelf()
        }

    private val handPaint = Paint()
        .apply {
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
        }

    private val handSubPaint = Paint()
        .apply {
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
        }

    private val tickPaint = Paint()
        .apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.WHITE
        }

    private val clockPaint = Paint()
        .apply {
            isAntiAlias = true
            color = Color.WHITE
        }

    private val textPaint = TextPaint()
        .apply {
            isAntiAlias = true
            color = Color.WHITE
        }

    init {
        textPaint.typeface = ResourcesCompat.getFont(context, R.font.manrope_semibold)
        clockPaint.typeface = ResourcesCompat.getFont(context, R.font.manrope_light)
    }

    override fun draw(canvas: Canvas) = canvas.performDraw()

    private fun Canvas.performDraw() {
        val surfaceColor = blendColor(
            ratio = ambience,
            // Transition from background color to black color
            // in ambient mode.
            a = backgroundAmbientColor,
            b = if (backgroundTintEnabled) {
                blendColor(0.15f, backgroundColor, accentColor)
            } else {
                backgroundColor
            },
        )
        drawColor(surfaceColor)

        val centerX = bounds.exactCenterX() + bounds.left
        val centerY = bounds.exactCenterY() + bounds.top
        val radius = min(
            bounds.exactCenterX(),
            bounds.exactCenterY(),
        )

        // Draw tick marks
        for (i in 0 until 60) {
            val degrees = 360f / 60f * i
            withRotation(
                degrees = degrees,
                pivotX = centerX,
                pivotY = centerY,
            ) {
                val offsetY = calculateSquareScreenOffsetY(degrees, radius)
                if (i.rem(5) == 0) {
                    tickPaint.alpha = 255
                    val length = blend(ambience, radius / 28f, 0f)
                    if (length > 0f) {
                        drawLine(centerX, offsetY, centerX, offsetY + length, tickPaint)
                    }
                } else {
                    tickPaint.alpha = 122
                    val length = blend(ambience, radius / 42f, 0f)
                    if (length > 0f) {
                        drawLine(centerX, offsetY, centerX, offsetY + length, tickPaint)
                    }
                }
            }
        }

        val strokeWidth = radius / 6f
        val hourHandLengthMax = (radius - strokeWidth) * HAND_HOUR_MAX_FACTOR
        val hourHandLengthMin = (radius - strokeWidth) * HAND_HOUR_MIN_FACTOR
        val hourHandLength = blend(ambience, hourHandLengthMax, hourHandLengthMin)
        val minuteHandLengthMax = (radius - strokeWidth) * HAND_MINUTE_MAX_FACTOR
        val minuteHandLengthMin = (radius - strokeWidth) * HAND_MINUTE_MIN_FACTOR
        val minuteHandLengthOffset = -calculateSquareScreenOffsetY(minuteHandRotation, radius) / 2f
        val minuteHandLength = blend(ambience, minuteHandLengthMax, minuteHandLengthMin) +
                minuteHandLengthOffset

        textPaint.color = contentColor
        textPaint.textAlign = Paint.Align.RIGHT
        textPaint.textSize = (radius - hourHandLength) / 2.3f
        textPaint.alpha = (30 * ambience).toInt()
        clockPaint.color = contentColor
        clockPaint.textAlign = Paint.Align.CENTER
        clockPaint.letterSpacing = -0.10f
        clockPaint.textSize = (radius - hourHandLengthMax) / 2f

        if (timeEnabled) {
            clockPaint.alpha = 225
            drawText(hours, centerX * 1.6f, centerY, clockPaint)
            clockPaint.alpha = 120
            drawText(minutes, centerX * 1.6f, centerY + clockPaint.textSize, clockPaint)
        }

        handPaint.color = accentColor
        handPaint.strokeWidth = radius / blend(ambience, 8f, 5f)
        handSubPaint.color = surfaceColor
        handSubPaint.strokeWidth = blend(ambience, radius / 14f, 0f)

        // Draw hour hand
        handPaint.alpha = 115
        drawClockHand(hourHandRotation, centerX, centerY, hourHandLength, handPaint)
        if (ambience < 1f)
            drawClockHand(hourHandRotation, centerX, centerY, hourHandLength / 1.5f, handSubPaint)

        // Draw minute hand
        handPaint.alpha = 255
        drawClockHand(minuteHandRotation, centerX, centerY, minuteHandLength, handPaint)
        if (ambience < 1f)
            drawClockHand(minuteHandRotation, centerX, centerY, hourHandLength / 1.5f, handSubPaint)

        if (timeEnabled) {
            clockPaint.alpha = 70
            drawText(hours, centerX * 1.6f, centerY, clockPaint)
            drawText(minutes, centerX * 1.6f, centerY + clockPaint.textSize, clockPaint)
        }

        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = (radius - minuteHandLengthMin) / 3.1f

        val textArcRadius = blend(ambience, radius * 1.1f, radius)
        WATCH_COMPLICATIONS.forEachIndexed { index, complicationId ->
            val value = complicationDataSparse?.get(complicationId)
                ?: return@forEachIndexed
            drawArc(
                rotation = 340f - 60f * index,
                centerX = centerX,
                centerY = centerY,
                radius = radius,
                length = textArcRadius,
                icon = value.icon,
                text = value.text?.toString().orEmpty(),
            )
        }
    }

    private fun calculateSquareScreenOffsetY(
        degrees: Float,
        radius: Float,
    ): Float {
        if (radius <= 0.1f || hasRoundScreen) return 0f
        val rad = degrees.rem(90f)
            .let {
                if (it > 45) {
                    90f - it
                } else it
            }
            .let { it.toDouble() }
            .let(Math::toRadians)
        val length = radius / cos(rad).toFloat()
        return radius - length
    }

    private fun Canvas.drawClockHand(
        rotation: Float,
        centerX: Float,
        centerY: Float,
        length: Float,
        paint: Paint,
    ) {
        withRotation(rotation, centerX, centerY) {
            drawLine(centerX, centerY, centerX, centerY - length, paint)
        }
    }

    private fun Canvas.drawArc(
        rotation: Float = 360f * ambience,
        centerX: Float,
        centerY: Float,
        radius: Float,
        length: Float,
        text: String? = null,
        icon: Drawable? = null,
    ) {
        val animation = ambience
        val direction = kotlin.run {
            val a = rotation.rem(360f) < 180f
            if (a) Path.Direction.CCW else Path.Direction.CW
        }
        withRotation(
            degrees = rotation,
            pivotX = centerX,
            pivotY = centerY,
        ) {
            if (icon != null)
                withRotation(
                    degrees = 90f + 6f,
                    pivotX = centerX,
                    pivotY = centerY,
                ) {
                    val bounds = icon.bounds
                    withTranslation(
                        x = centerX - bounds.width() / 2f,
                        y = radius - length + textPaint.fontMetrics.leading,
                    ) {
                        withRotation(
                            degrees = (if (direction == Path.Direction.CW) 0f else 1f) * 180f,
                            pivotX = bounds.width() / 2f,
                            pivotY = bounds.height() / 2f
                        ) {
                            icon.alpha = (255f * animation).toInt()
                            icon.draw(this)
                        }
                    }
                }

            textPaint.textAlign =
                if (direction == Path.Direction.CW) Paint.Align.RIGHT else Paint.Align.LEFT
            val path = Path().apply {
                val offsetY = if (direction == Path.Direction.CW) {
                    textPaint.fontMetrics.top * 1.2f
                } else {
                    -textPaint.fontMetrics.bottom - textPaint.fontMetrics.leading
                }
                addCircle(
                    centerX,
                    centerY,
                    length + offsetY,
                    direction
                )
            }
            textPaint.alpha = (255f * animation).toInt()
            drawTextOnPath(text.orEmpty(), path, 0f, 0f, textPaint)
        }
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        error("Unsupported")
    }

    override fun setAlpha(alpha: Int) {
        error("Unsupported")
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

}

fun AnalogClockDrawable.installCfgIn(scope: CoroutineScope, invalidate: () -> Unit) {
    Cfg
        .asFlowOfProperty<String>(Cfg.KEY_THEME)
        .onEach { themeName ->
            val theme = when (themeName) {
                Cfg.THEME_BLACK -> Theme.BLACK
                Cfg.THEME_DARK -> Theme.DARK
                Cfg.THEME_LIGHT -> Theme.LIGHT
                else -> Theme.BLACK
            }
            backgroundColor = theme.backgroundColor
            contentColor = theme.contentColor
            invalidate()
        }
        .launchIn(scope)
    Cfg
        .asFlowOfProperty<Boolean>(Cfg.KEY_DIGITAL_CLOCK_ENABLED)
        .onEach { enabled ->
            timeEnabled = enabled
            invalidate()
        }
        .launchIn(scope)
    Cfg
        .asFlowOfProperty<Boolean>(Cfg.KEY_ACCENT_BG_ENABLED)
        .onEach { enabled ->
            backgroundTintEnabled = enabled
            invalidate()
        }
        .launchIn(scope)
    Cfg
        .asFlowOfProperty<Int>(Cfg.KEY_ACCENT_COLOR)
        .onEach { color ->
            accentColor = color
            invalidate()
        }
        .launchIn(scope)
}

fun AnalogClockDrawable.installAmbientIn(
    scope: CoroutineScope,
    ambientFlow: Flow<Boolean>,
    invalidate: () -> Unit,
) {
    var animator: Animator? = null
    ambientFlow
        .onEach { inAmbientMode ->
            animator?.cancel()
            animator = ValueAnimator
                .ofFloat(0f, 1f)
                .apply {
                    addUpdateListener {
                        val r = it.animatedValue as Float
                        ambience = if (inAmbientMode) 1f - r else r
                        invalidate()
                    }

                    interpolator = TimeInterpolator { sin(it * PI * 0.5f).toFloat() }
                    duration = 300L
                    start()
                }
        }
        .launchIn(scope)
        .invokeOnCompletion {
            animator?.cancel()
            animator = null
        }
}

fun AnalogClockDrawable.installTimeIn(
    scope: CoroutineScope,
    timeFlow: Flow<Time>,
    ambientFlow: StateFlow<Boolean>,
    invalidate: () -> Unit,
) {
    var animator: Animator? = null
    timeFlow
        .onEach { t ->
            val date = Date(t.millis)
            val calendar = Calendar.getInstance().apply {
                time = date
            }

            // Update digital clock
            this.hours = formatTwoDigitNumber(
                if (is24HourFormat(context)) {
                    calendar.get(Calendar.HOUR_OF_DAY)
                } else {
                    calendar.get(Calendar.HOUR)
                        .takeIf { it != 0 }
                        ?: 12
                }
            )
            this.minutes = formatTwoDigitNumber(calendar.get(Calendar.MINUTE))

            // Update analog clock
            animator?.cancel()
            animator = showAnalogTime(
                calendar = calendar,
                animate = !ambientFlow.value,
                invalidate = invalidate,
            )

            // Render
            invalidate()
        }
        .launchIn(scope)
        .invokeOnCompletion {
            animator?.cancel()
            animator = null
        }
}

private fun calculateHourHandRotation(time: Int) = (time / 2f) % 360f

private fun calculateMinuteHandRotation(time: Int) = (time % 60f) * 6f

private fun AnalogClockDrawable.showAnalogTime(
    calendar: Calendar,
    animate: Boolean,
    invalidate: () -> Unit,
): Animator? {
    val time = calendar.get(Calendar.MINUTE) + calendar.get(Calendar.HOUR_OF_DAY) * 60
    val hourHandRotationNew = calculateHourHandRotation(time)
    val minuteHandRotationNew = calculateMinuteHandRotation(time)

    if (animate) {
        fun rotationDelta(new: Float, old: Float) =
            (new - old).let { dt ->
                if (dt < 360f - dt) {
                    // Rotate forwards
                    dt
                } else {
                    // Rotate backwards
                    dt - 360f
                }
            }

        val hourHandRotationOld = hourHandRotation % 360f
        val minuteHandRotationOld = minuteHandRotation % 360f
        val hourHandRotationDelta =
            rotationDelta(hourHandRotationNew, hourHandRotationOld)
        val minuteHandRotationDelta =
            rotationDelta(minuteHandRotationNew, minuteHandRotationOld)

        // Animate analog clock changing
        // time.
        return ValueAnimator
            .ofFloat(0f, 1f)
            .apply {
                addUpdateListener {
                    val r = it.animatedValue as Float
                    hourHandRotation = hourHandRotationOld + hourHandRotationDelta * r
                    minuteHandRotation = minuteHandRotationOld + minuteHandRotationDelta * r
                    invalidate()
                }

                interpolator = TimeInterpolator { sin(it * PI * 0.5f).toFloat() }
                duration = 200L
                start()
            }
    } else {
        hourHandRotation = hourHandRotationNew
        minuteHandRotation = minuteHandRotationNew
        return null
    }
}

/**
 * Formats number as two-digit number: adds leading zero if
 * needed.
 */
private fun formatTwoDigitNumber(n: Int) = if (n <= 9) "0$n" else "$n"
