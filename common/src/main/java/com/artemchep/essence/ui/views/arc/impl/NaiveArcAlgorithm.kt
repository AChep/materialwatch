package com.artemchep.essence.ui.views.arc.impl

import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.get
import com.artemchep.essence.ui.views.arc.ArcAlgorithm
import kotlin.math.*

class NaiveArcAlgorithm : ArcAlgorithm {

    companion object {
        private const val TAG = "NaiveArcAlgorithm"
    }

    override fun prepare(bitmap: Bitmap) {
    }

    override fun bend(bitmap: Bitmap): Bitmap {
        val new = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
            .apply {
                val then = SystemClock.elapsedRealtime()

                for (u in 0 until bitmap.width) {
                    for (v in 0 until bitmap.height) {
                        val radius = sqrt(u * u + v * v + 0.0) + 0.0001
                        val ratio = asin(u.toDouble() / radius) / PI * 2.0

                        val x = ratio * width
                        val y = radius
                        if (0 <= x && x <= bitmap.width - 1 &&
                            0 <= y && y <= bitmap.height - 1
                        ) {
                            val xCeil = ceil(x).toInt()
                            val xFloor = floor(x).toInt()
                            val yCeil = ceil(y).toInt()
                            val yFloor = floor(y).toInt()

                            val colorCeil = ColorUtils.blendARGB(
                                bitmap[xCeil, yCeil],
                                bitmap[xFloor, yCeil],
                                (1.0f - x + xFloor).toFloat()
                            )
                            val colorFloor = ColorUtils.blendARGB(
                                bitmap[xCeil, yFloor],
                                bitmap[xFloor, yFloor],
                                (1.0f - x + xFloor).toFloat()
                            )
                            val color = ColorUtils.blendARGB(
                                colorCeil,
                                colorFloor,
                                (1.0f - y + yFloor).toFloat()
                            )
                            setPixel(u, v, color)
                        }
                    }
                }

                val now = SystemClock.elapsedRealtime()
                Log.i(TAG, "Spent ${now - then}ms to curve a quart")
            }
        return new
    }

    override fun recycle() {
    }
}