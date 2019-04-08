package com.artemchep.essence.ui.format

import com.artemchep.essence.domain.models.Temperature
import kotlin.math.roundToInt

fun formatRich(temp: Temperature) = "${String.format("%.1f", temp.c)}°C"

fun format(temp: Temperature) = "${temp.c.roundToInt()}°"
