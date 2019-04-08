package com.artemchep.essence.ui.views.arc

import android.graphics.Bitmap

/**
 * @author Artem Chepurnoy
 */
interface ArcAlgorithm {
    fun prepare(bitmap: Bitmap)

    fun bend(bitmap: Bitmap): Bitmap

    fun recycle()
}
