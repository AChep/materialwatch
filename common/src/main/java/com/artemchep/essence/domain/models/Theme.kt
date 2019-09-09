package com.artemchep.essence.domain.models

import android.graphics.Color
import arrow.optics.optics

/**
 * @author Artem Chepurnoy
 */
@optics
data class Theme(
    /**
     * The color of background of
     * the watch-face.
     */
    var backgroundColor: Int = 0,
    var complicationColor: Int = 0,
    var clockHourColor: Int = 0,
    var clockHourOutline: Boolean = false,
    var clockMinuteColor: Int = 0,
    var isAntialias: Boolean = true
) {

    companion object {
        val BLACK = Theme(
            backgroundColor = Color.BLACK,
            clockHourColor = 0xFF546E7A.toInt(),
            clockMinuteColor = Color.GRAY,
            complicationColor = Color.WHITE
        )
        val DARK = Theme(
            backgroundColor = 0xFF212121.toInt(),
            clockHourColor = 0xFF546E7A.toInt(),
            clockMinuteColor = Color.GRAY,
            complicationColor = Color.WHITE
        )
        val LIGHT = Theme(
            backgroundColor = 0xFFF5F5F5.toInt(),
            clockHourColor = 0xFF78909C.toInt(),
            clockMinuteColor = Color.GRAY,
            complicationColor = Color.BLACK
        )
        val AMBIENT = Theme(
            backgroundColor = Color.BLACK,
            clockHourColor = Color.WHITE,
            clockHourOutline = true,
            clockMinuteColor = Color.GRAY,
            complicationColor = Color.WHITE
        )
    }

}