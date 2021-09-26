package com.artemchep.essence.ui.util

import androidx.core.graphics.ColorUtils

fun blend(ratio: Float, a: Float, b: Float) = (a) * (1f - ratio) + (b) * ratio

fun blendColor(ratio: Float, a: Int, b: Int) = ColorUtils.blendARGB(a, b, ratio)
