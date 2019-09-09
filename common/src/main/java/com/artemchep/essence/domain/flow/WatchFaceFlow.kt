package com.artemchep.essence.domain.flow

import android.graphics.drawable.Drawable
import arrow.core.Either
import arrow.core.Option
import arrow.core.left
import arrow.core.toOption
import arrow.optics.Optional
import arrow.optics.optics
import com.artemchep.essence.domain.exceptions.NoDataException
import com.artemchep.essence.domain.models.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

private const val DEBOUNCE_COMPLICATIONS_MS = 36L

@UseExperimental(ExperimentalCoroutinesApi::class)
fun WatchFaceFlow(
    timeFlow: Flow<Time>,
    themeFlow: Flow<Theme>,
    visibilityFlow: Flow<Visibility>,
    weatherFlow: Flow<Either<Throwable, Weather>>,
    complicationFlow: Flow<Map<Int, Pair<Drawable?, String?>>>
): Flow<WatchFaceDelta<*>> {
    val weatherFlowWithStart = weatherFlow.onStart { emit(NoDataException().left()) }
    val complicationFlowWithStart = complicationFlow.onStart { emit(emptyMap()) }
    return timeFlow
        .map {
            // Create a watch face builder from a time, and populate it
            // down the pipe.
            WatchFaceBuilder(time = it)
        }
        .combine(themeFlow) { builder, theme ->
            builder.copy(theme = theme)
        }
        .combine(visibilityFlow) { builder, visibility ->
            builder.copy(visibility = visibility)
        }
        .combine(weatherFlowWithStart) { builder, weather ->
            builder.copy(weather = weather)
        }
        .combine(
            complicationFlowWithStart
                .debounce(DEBOUNCE_COMPLICATIONS_MS)
        ) { builder, complications ->
            builder.copy(complications = complications)
        }
        .delta()
}

@optics
data class WatchFaceBuilder(
    val theme: Theme? = null,
    val visibility: Visibility? = null,
    /**
     * Either error message or an actual
     * weather.
     */
    val weather: Either<Throwable, Weather>? = null,
    val time: Time? = null,
    val complications: Map<Int, Pair<Drawable?, String?>>? = null
) {
    companion object
}

private fun Flow<WatchFaceBuilder>.delta(): Flow<WatchFaceDelta<*>> =
    flow {
        var previousValue: WatchFaceBuilder? = null

        fun <T> Optional<WatchFaceBuilder, T>.emptyIfNotChanged(
            old: WatchFaceBuilder?,
            new: WatchFaceBuilder
        ): Option<T> =
            getOption(new)
                .map {
                    if (old == null || getOption(old) != it) {
                        it
                    } else {
                        null
                    }
                }
                .flatMap { it.toOption() }

        collect { value ->
            suspend fun <T> emitIfChanged(
                lens: Optional<WatchFaceBuilder, T>,
                factory: (T) -> WatchFaceDelta<T>
            ) = lens
                .emptyIfNotChanged(previousValue, value)
                .map { value ->
                    val delta = factory(value)
                    emit(delta as WatchFaceDelta<*>)
                }

            emitIfChanged(WatchFaceBuilder.visibility, ::WatchFaceVisibility)
            emitIfChanged(WatchFaceBuilder.weather, ::WatchFaceWeather)
            emitIfChanged(WatchFaceBuilder.time, ::WatchFaceTime)
            emitIfChanged(WatchFaceBuilder.theme, ::WatchFaceTheme)
            emitIfChanged(WatchFaceBuilder.complications, ::WatchFaceComplication)

            previousValue = value
        }
    }
