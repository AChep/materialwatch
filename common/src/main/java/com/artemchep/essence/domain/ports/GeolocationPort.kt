package com.artemchep.essence.domain.ports

import arrow.core.Either
import com.artemchep.essence.domain.models.Geolocation

/**
 * @author Artem Chepurnoy
 */
interface GeolocationPort {
    suspend fun getGeolocation(): Either<Throwable, Geolocation>
}
