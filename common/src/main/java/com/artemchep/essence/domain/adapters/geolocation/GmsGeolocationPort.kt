package com.artemchep.essence.domain.adapters.geolocation

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.WorkerThread
import arrow.core.Either
import arrow.core.toOption
import com.artemchep.essence.domain.exceptions.GeolocationAccessException
import com.artemchep.essence.domain.exceptions.GeolocationEmptyException
import com.artemchep.essence.domain.models.Geolocation
import com.artemchep.essence.domain.ports.GeolocationPort
import com.artemchep.essence.extensions.await
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutionException

/**
 * @author Artem Chepurnoy
 */
class GmsGeolocationPort(context: Context) : GeolocationPort {
    private val client: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context.applicationContext)

    @SuppressLint("MissingPermission")
    @WorkerThread
    override suspend fun getGeolocation(): Either<Throwable, Geolocation> {
        return try {
            withContext(Dispatchers.IO) {
                client.lastLocation.await()
            }
                .toOption()
                .map {
                    val latitude = it.latitude
                    val longitude = it.longitude
                    Geolocation(latitude, longitude)
                }
                .toEither {
                    GeolocationEmptyException()
                }
        } catch (e: Exception) {
            when (e) {
                is SecurityException,
                is ExecutionException -> GeolocationAccessException(e)
                else -> e
            }.let {
                Either.Left(it)
            }
        }
    }
}