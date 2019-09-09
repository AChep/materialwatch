package com.artemchep.essence.domain.models

import android.graphics.drawable.Drawable
import arrow.core.Either

/**
 * @author Artem Chepurnoy
 */
data class WatchFace(
    val theme: Theme,
    val visibility: Visibility,
    /**
     * Either error message or an actual
     * weather.
     */
    val weather: Either<Throwable, Weather>,
    val time: Time,
    val complications: Map<Int, Pair<Drawable?, String?>>
)
