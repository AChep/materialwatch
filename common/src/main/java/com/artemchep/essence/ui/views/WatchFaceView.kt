package com.artemchep.essence.ui.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.SystemClock
import android.text.format.DateFormat
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import arrow.core.Either
import com.artemchep.essence.*
import com.artemchep.essence.domain.exceptions.ApiLimitReachedException
import com.artemchep.essence.domain.exceptions.GeolocationAccessException
import com.artemchep.essence.domain.exceptions.GeolocationEmptyException
import com.artemchep.essence.domain.exceptions.NoDataException
import com.artemchep.essence.domain.models.*
import com.artemchep.essence.ui.format.format
import com.artemchep.essence.ui.format.formatRich
import com.artemchep.essence.ui.views.arc.ArcLayout
import com.artemchep.essence_common.R
import com.artemchep.essence_common.databinding.WatchFaceBinding
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

    companion object {
        private const val TAG = "WatchFaceView"
    }

    private val iconSize by lazy { context.resources.getDimensionPixelSize(R.dimen.watch_face_icon_size) }

    private val binding by lazy {
        WatchFaceBinding.bind(this)
    }

    private var themePrev: Theme? = null

    private var weatherPrev: Either<Throwable, Weather>? = null

    override fun hasOverlappingRendering(): Boolean = false

    fun setDelta(delta: WatchFaceDelta<*>) {
        when (delta) {
            is WatchFaceTheme -> setTheme(delta.value)
            is WatchFaceTime -> setTime(delta.value)
            is WatchFaceVisibility -> setVisibility(delta.value)
            is WatchFaceWeather -> setWeather(delta.value)
            is WatchFaceComplication -> setComplications(delta.value)
        }
    }

    fun setTheme(theme: Theme) {
        if (themePrev == theme) {
            return
        }

        setBackgroundColor(theme.backgroundColor)
        setThemeTime(theme)
        setThemeComplication(theme)

        themePrev = theme
    }

    private fun setThemeTime(theme: Theme) {
        binding.minute.apply {
            paint.isAntiAlias = theme.isAntialias
            setTextColor(theme.clockMinuteColor)
        }

        binding.hour.apply {
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
    }

    private fun setThemeComplication(theme: Theme) {
        val tintList = ColorStateList.valueOf(theme.complicationColor)
        listOf(
            binding.complication1TextView,
            binding.complication2TextView,
            binding.complication3TextView,
            binding.complication4TextView,
            binding.complication5TextView,
            binding.complication6TextView,
            binding.tempCurIconView,
            binding.tempCurTextView
        ).forEach {
            val hasChanged = it.textColors !== tintList ||
                    it.paint.isAntiAlias != theme.isAntialias

            TextViewCompat.setCompoundDrawableTintList(it, tintList)
            it.setTextColor(tintList)
            it.paint.isAntiAlias = theme.isAntialias

            if (hasChanged) {
                // Force the re-render of the
                // arc layout.
                it.getParentArcLayoutAndRetainInTag().clearBitmapCache()
                it.getParentArcLayoutAndRetainInTag().invalidate()
            }
        }
    }

    fun setTime(time: Time) {
        val calendar = Calendar.getInstance().apply {
            setTime(Date(time.millis))
        }

        binding.minute.text = formatTwoDigitNumber(calendar.get(Calendar.MINUTE))
        binding.hour.text = formatTwoDigitNumber(if (DateFormat.is24HourFormat(context)) {
            calendar.get(Calendar.HOUR_OF_DAY)
        } else calendar.get(Calendar.HOUR).takeIf { it != 0 } ?: 12)
    }

    /**
     * Formats number as two-digit number: adds leading zero if
     * needed.
     */
    private fun formatTwoDigitNumber(n: Int) = if (n <= 9) "0$n" else "$n"

    fun setWeather(weather: Either<Throwable, Weather>) {
        fun setNoTodayWeather() {
            binding.tempMinView.isVisible = false
            binding.tempMaxView.isVisible = false
            binding.tempProgressView.progress = 0
        }

        when (weather) {
            is Either.Right -> weather.value.apply {
                // Format current weather
                binding.tempCurIconView.isVisible = current != null
                if (current != null) {
                    binding.tempCurTextView.text = formatRich(current.temp)
                }

                // Format today weather
                if (today != null) {
                    binding.tempMinView.isVisible = true
                    binding.tempMaxView.isVisible = true
                    binding.tempMinView.text = format(today.tempMin)
                    binding.tempMaxView.text = format(today.tempMax)

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
                    binding.tempProgressView.progress =
                        (progress * binding.tempProgressView.max).roundToInt()
                    binding.tempProgressView.progressDrawable
                        .let { it as LayerDrawable }
                        .findDrawableByLayerId(android.R.id.progress)
                        .let {
                            val startColor = 0xFF64c1ff.toInt()
                            val endColor = 0xFFffff52.toInt()
                            it.setTint(ColorUtils.blendARGB(startColor, endColor, progress))
                        }
                } else {
                    setNoTodayWeather()
                }
            }
            is Either.Left -> weather.value.apply {
                binding.tempCurTextView.text = when (this) {
                    is ApiLimitReachedException -> R.string.error_api_limit_reached
                    is GeolocationAccessException -> R.string.error_geolocation_access
                    is GeolocationEmptyException -> R.string.error_geolocation_empty
                    is NoDataException -> null
                    else -> R.string.error_no_internet
                }
                    ?.let(context::getString)
                    .orEmpty()
                binding.tempCurIconView.isVisible = false
                setNoTodayWeather()
            }
        }

        val hasChanged = weatherPrev
            ?.fold(
                ifLeft = { e ->
                    weather.swap().exists { it.javaClass == e.javaClass }
                },
                ifRight = { w ->
                    weather.exists { it == w }
                },
            ) != true
        if (hasChanged) {
            // Request to redraw the arc
            // layout
            binding.tempCurTextView.getParentArcLayoutAndRetainInTag().clearBitmapCache()
        }

        weatherPrev = weather
    }

    fun setVisibility(visibility: Visibility) {
        binding.arcTopStart.isVisible = visibility.isTopStartVisible
        binding.arcTopEnd.isVisible = visibility.isTopEndVisible
        binding.arcBottomStart.isVisible = visibility.isBottomStartVisible
        binding.arcBottomEnd.isVisible = visibility.isBottomEndVisible
    }

    fun setComplications(complications: Map<Int, Pair<Drawable?, String?>>) {
        complications.entries.forEach { (id, value) ->
            val complicationView = findComplicationViewById(id)
            with(complicationView) {
                val hasChanged =
                    setComplicationIcon(value.first)
                        .or(setComplicationContentText(value.second))
                if (hasChanged) {
                    getParentArcLayoutAndRetainInTag().clearBitmapCache()
                }
            }
        }
    }

    private fun findComplicationViewById(id: Int) = when (id) {
        WATCH_COMPLICATION_FIRST -> binding.complication1TextView
        WATCH_COMPLICATION_SECOND -> binding.complication2TextView
        WATCH_COMPLICATION_THIRD -> binding.complication5TextView
        WATCH_COMPLICATION_FOURTH -> binding.complication6TextView
        WATCH_COMPLICATION_FIFTH -> binding.complication3TextView
        WATCH_COMPLICATION_SIXTH -> binding.complication4TextView
        else -> throw IllegalArgumentException("Unknown watch face complication id [$id]")
    }

    private fun View.getParentArcLayout(): ArcLayout {
        return if (this is ArcLayout) this else (parent as View).getParentArcLayout()
    }

    private fun View.getParentArcLayoutAndRetainInTag(): ArcLayout {
        return tag as? ArcLayout ?: getParentArcLayout().also(::setTag)
    }

    /**
     * Sets the complication icon to start of the view
     * @see setComplicationContentText
     */
    private fun TextView.setComplicationIcon(icon: Drawable?): Boolean {
        val drawablePrev = compoundDrawables.firstOrNull { it != null }
        if (drawablePrev === icon) {
            return false
        }

        val drawable = icon?.applyIconBounds()
        this.setCompoundDrawables(drawable, null, null, null)
        return true
    }

    /**
     * Sets the complication text, or hides the view
     * if text is `null`.
     * @see setComplicationIcon
     */
    private fun TextView.setComplicationContentText(text: CharSequence?): Boolean {
        val trimmedText = text?.trim()
        val trimmedTextPrev = this.text
        if (trimmedTextPrev == trimmedText) {
            return false
        }

        this.isVisible = !trimmedText.isNullOrEmpty()
        this.text = trimmedText
        return true
    }

    /**
     * Applies the {0, 0, [iconSize], [iconSize]}
     * bounds to the drawable.
     */
    private fun Drawable.applyIconBounds(): Drawable {
        setBounds(0, 0, (iconSize * 0.8).toInt(), iconSize)
        return this
    }

    // ---- Rendering ----

    override fun draw(canvas: Canvas?) {
        val beginTime = SystemClock.currentThreadTimeMillis()

        super.draw(canvas)

        val endTime = SystemClock.currentThreadTimeMillis()
        Log.i(TAG, "Drawing a watch face took ${endTime - beginTime}ms.")
    }

}
