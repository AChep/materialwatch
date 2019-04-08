package com.artemchep.essence.ui.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.text.format.DateFormat
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import arrow.core.Either
import com.artemchep.essence.*
import com.artemchep.essence.domain.exceptions.ApiLimitReachedException
import com.artemchep.essence.domain.exceptions.GeolocationAccessException
import com.artemchep.essence.domain.exceptions.GeolocationEmptyException
import com.artemchep.essence.domain.models.Theme
import com.artemchep.essence.domain.models.Time
import com.artemchep.essence.domain.models.Visibility
import com.artemchep.essence.domain.models.Weather
import com.artemchep.essence.ui.format.format
import com.artemchep.essence.ui.format.formatRich
import com.artemchep.essence_common.R
import kotlinx.android.synthetic.main.watch_face.view.*
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * @author Artem Chepurnoy
 */
class WatchFaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var iconSize: Int = 0

    override fun onFinishInflate() {
        super.onFinishInflate()
        WATCH_COMPLICATIONS.forEach { id ->
            findComplicationViewById(id).tag = id
        }

        iconSize = context.resources.getDimensionPixelSize(R.dimen.watch_face_icon_size)
    }

    override fun hasOverlappingRendering(): Boolean = false

    /**
     * Enables or disables the anti-aliasing of all of the
     * text views.
     */
    fun setAntiAlias(isEnabled: Boolean) {
        listOf(
            hour, minute,
            complication1TextView,
            complication2TextView,
            complication3TextView,
            complication4TextView,
            complication5TextView,
            complication6TextView
        ).forEach {
            it.paint.isAntiAlias = isEnabled
        }
    }

    fun setTheme(theme: Theme) {
        setBackgroundColor(theme.backgroundColor)

        minute.apply {
            paint.isAntiAlias = theme.isAntialias
            setTextColor(theme.clockMinuteColor)
        }

        hour.apply {
            paint.isAntiAlias = theme.isAntialias
            setStrokeColor(theme.clockHourColor)

            if (theme.clockHourOutline) {
                setTextColor(theme.backgroundColor)
                setStrokeWidth(TypedValue.COMPLEX_UNIT_DIP, 1.5f)
            } else {
                setTextColor(theme.clockHourColor)
                setStrokeWidth(0f)
            }
        }

        // Set complications color
        val tintList = ColorStateList.valueOf(theme.complicationColor)
        listOf(
            complication1TextView,
            complication2TextView,
            complication3TextView,
            complication4TextView,
            complication5TextView,
            complication6TextView,
            tempCurIconView,
            tempCurTextView
        ).forEach {
            it.setTextColor(theme.complicationColor)
            it.compoundDrawableTintList = tintList
            it.paint.isAntiAlias = theme.isAntialias
        }
    }

    fun setTime(time: Time) {
        val calendar = Calendar.getInstance().apply {
            setTime(Date(time.millis))
        }

        minute.text = formatTwoDigitNumber(calendar.get(Calendar.MINUTE))
        hour.text = formatTwoDigitNumber(if (DateFormat.is24HourFormat(context)) {
            calendar.get(Calendar.HOUR_OF_DAY)
        } else calendar.get(Calendar.HOUR).takeIf { it != 0 } ?: 12)
    }

    /**
     * Formats number as two-digit number: adds leading zero if
     * needed.
     */
    private fun formatTwoDigitNumber(n: Int) = if (n <= 9) "0$n" else "$n"

    fun setWeather(weather: Either<Throwable, Weather>) {
        when (weather) {
            is Either.Right -> weather.b.apply {
                // Format current weather
                tempCurIconView.isVisible = current != null
                if (current != null) {
                    tempCurTextView.text = formatRich(current.temp)
                }

                // Format today weather
                if (today != null) {
                    tempMinView.text = format(today.tempMin)
                    tempMaxView.text = format(today.tempMax)

                    val progress = if (current != null) {
                        val tempCur = max(min(current.temp.c, today.tempMax.c), today.tempMin.c)
                        val width = today.tempMax.c - today.tempMin.c
                        if (width == 0f) {
                            1f
                        } else {
                            (tempCur - today.tempMin.c) / width
                        }
                    } else {
                        1f
                    }
                    tempProgressView.progress = (progress * tempProgressView.max).roundToInt()
                    tempProgressView.progressDrawable
                        .let { it as LayerDrawable }
                        .let { it.findDrawableByLayerId(android.R.id.progress) }
                        .let {
                            val startColor = 0xFF64c1ff.toInt()
                            val endColor = 0xFFffff52.toInt()
                            it.setTint(ColorUtils.blendARGB(startColor, endColor, progress))
                        }
                }
            }
            is Either.Left -> weather.a.apply {
                tempCurTextView.text = when (this) {
                    is ApiLimitReachedException -> "API limit error"
                    is GeolocationAccessException -> "Access denied error"
                    is GeolocationEmptyException -> "Unknown location error"
                    else -> {
                        "IO error"
                    }
                }
                tempCurIconView.isVisible = false
            }
        }
    }

    fun setVisibility(visibility: Visibility) {
        arcTopStart.isVisible = visibility.isTopStartVisible
        arcTopEnd.isVisible = visibility.isTopEndVisible
        arcBottomStart.isVisible = visibility.isBottomStartVisible
        arcBottomEnd.isVisible = visibility.isBottomEndVisible
    }

    fun setComplications(complications: Map<Int, Pair<Drawable?, String?>>) {
        complications.entries.forEach { (id, value) ->
            val complicationView = findComplicationViewById(id)
            with(complicationView) {
                setComplicationIcon(value.first)
                setComplicationContentText(value.second)
            }
        }
    }

    private fun findComplicationViewById(id: Int) = when (id) {
        WATCH_COMPLICATION_FIRST -> complication1TextView
        WATCH_COMPLICATION_SECOND -> complication2TextView
        WATCH_COMPLICATION_THIRD -> complication5TextView
        WATCH_COMPLICATION_FOURTH -> complication6TextView
        WATCH_COMPLICATION_FIFTH -> complication3TextView
        WATCH_COMPLICATION_SIXTH -> complication4TextView
        else -> throw IllegalArgumentException("Unknown watch face complication id [$id]")
    }

    /**
     * Sets the complication icon to start of the view
     * @see setComplicationContentText
     */
    private fun TextView.setComplicationIcon(icon: Drawable?) {
        val drawable = icon?.applyIconBounds()
        this.setCompoundDrawables(drawable, null, null, null)
    }

    /**
     * Sets the complication text, or hides the view
     * if text is `null`.
     * @see setComplicationIcon
     */
    private fun TextView.setComplicationContentText(text: CharSequence?) {
        val trimmedText = text?.trim()
        this.isVisible = !trimmedText.isNullOrEmpty()
        this.text = trimmedText
    }

    /**
     * Applies the {0, 0, [iconSize], [iconSize]}
     * bounds to the drawable.
     */
    private fun Drawable.applyIconBounds(): Drawable {
        setBounds(0, 0, (iconSize * 0.8).toInt(), iconSize)
        return this
    }

}
