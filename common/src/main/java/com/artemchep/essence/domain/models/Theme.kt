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
    val backgroundColor: Int = 0,
    val contentColor: Int = 0,
) {

    companion object {
        val BLACK = Theme(
            backgroundColor = Color.BLACK,
            contentColor = Color.WHITE
        )
        val DARK = Theme(
            backgroundColor = 0xFF212121.toInt(),
            contentColor = Color.WHITE
        )
        val LIGHT = Theme(
            backgroundColor = 0xFFF5F5F5.toInt(),
            contentColor = Color.BLACK
        )
    }

}