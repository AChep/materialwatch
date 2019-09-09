package com.artemchep.essence.domain.flow

import arrow.core.Either
import com.artemchep.essence.domain.models.Geolocation
import com.artemchep.essence.domain.models.Moment
import com.artemchep.essence.domain.models.Time
import com.artemchep.essence.domain.ports.GeolocationPort
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*

@UseExperimental(ExperimentalCoroutinesApi::class)
fun GeolocationPort.asFlow(
    permissionsChangedFlow: Flow<Unit>,
    periodFlow: Flow<Long>,
    timeFlow: Flow<Time>
): Flow<Moment<Either<Throwable, Geolocation>>> {
    var timeLastSuccess: Long = 0

    return flowOf(
        timeFlow
            .combine(periodFlow) { time, periodMs ->
                // Returns a unit if we should fetch a new geolocation data,
                // null otherwise.
                if (time.millis - timeLastSuccess > periodMs) {
                    Unit
                } else {
                    null
                }
            }
            .filterNotNull(),
        permissionsChangedFlow
    )
        .flattenMerge()
        .map {
            coroutineScope {
                getGeolocation()
            }.let { Moment.now(it) }
        }
        .onEach {
            // Remember the time, cause we did get the location from
            // a client.
            if (it.unbox().isRight()) {
                timeLastSuccess = it.time.millis
            }
        }
}
