package com.artemchep.essence.domain.models

import android.graphics.drawable.Drawable
import arrow.core.Either

/**
 * @author Artem Chepurnoy
 */
sealed class WatchFaceDelta<T>(val value: T)

class WatchFaceTheme(theme: Theme) :
    WatchFaceDelta<Theme>(theme)

class WatchFaceVisibility(visibility: Visibility) :
    WatchFaceDelta<Visibility>(visibility)

class WatchFaceWeather(weather: Either<Throwable, Weather>) :
    WatchFaceDelta<Either<Throwable, Weather>>(weather)

class WatchFaceTime(time: Time) :
    WatchFaceDelta<Time>(time)

class WatchFaceComplication(map: Map<Int, Pair<Drawable?, String?>>) :
    WatchFaceDelta<Map<Int, Pair<Drawable?, String?>>>(map)
