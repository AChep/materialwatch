package com.artemchep.essence.domain.flow

import arrow.core.Either
import arrow.core.flatMap
import com.artemchep.essence.domain.models.Geolocation
import com.artemchep.essence.domain.models.Moment
import com.artemchep.essence.domain.models.Time
import com.artemchep.essence.domain.models.Weather
import com.artemchep.essence.domain.ports.WeatherPort
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@UseExperimental(ExperimentalCoroutinesApi::class)
fun WeatherPort.asFlow(
    periodFlow: Flow<Long>,
    geolocationFlow: Flow<Either<Throwable, Geolocation>>,
    timeFlow: Flow<Time>
): Flow<Moment<Either<Throwable, Weather>>> {
    var timeLastSuccess: Long = 0
    return timeFlow
        .combine(periodFlow) { time, periodMs ->
            // Returns time if we should fetch a new geolocation data,
            // null otherwise.
            if (time.millis - timeLastSuccess > periodMs) {
                time
            } else {
                null
            }
        }
        .filterNotNull()
        .combine(geolocationFlow) { _, geolocation -> geolocation }
        .map {
            it
                .flatMap { geolocation ->
                    getWeather(geolocation)
                }
                .let {
                    Moment.now(it)
                }
        }
        .onEach {
            // Remember the time, cause we did get the weather.
            if (it.unbox().isRight()) {
                timeLastSuccess = it.time.millis
            }
        }
}
