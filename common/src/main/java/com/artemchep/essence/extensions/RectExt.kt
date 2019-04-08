package com.artemchep.essence.extensions

import android.graphics.Rect
import kotlin.math.min

val Rect.radius: Float
    get() = min(exactCenterX(), exactCenterY())