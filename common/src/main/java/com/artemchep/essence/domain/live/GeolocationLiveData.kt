package com.artemchep.essence.domain.live

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.WorkerThread
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import arrow.core.Either
import arrow.core.toOption
import com.artemchep.essence.ACTION_PERMISSIONS_CHANGED
import com.artemchep.essence.Cfg
import com.artemchep.essence.domain.exceptions.GeolocationAccessException
import com.artemchep.essence.domain.exceptions.GeolocationEmptyException
import com.artemchep.essence.domain.live.base.Live3
import com.artemchep.essence.domain.models.Geolocation
import com.artemchep.essence.domain.models.Moment
import com.artemchep.essence.domain.models.Time
import com.artemchep.essence.extensions.await
import com.artemchep.essence.extensions.produce
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutionException

/**
 * @author Artem Chepurnoy
 */
class GeolocationLiveData(
    private val context: Context,
    private val config: Cfg,
    /**
     * The emitter of the time
     */
    private val timeLiveData: Live3<Time>
) : Live3<Moment<Either<Throwable, Geolocation>>>(Moment.now(Either.left(GeolocationEmptyException()))) {

    companion object {
        private const val TAG = "GeolocationLiveData"
    }

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private var geolocationJob: Job? = null

    override fun onActive() {
        super.onActive()
        launch {
            timeLiveData.openSubscription(this)
                .consumeEach {
                    if (value.unbox().let { e ->
                            e is Either.Left &&
                                    (e.a is GeolocationEmptyException ||
                                            e.a is GeolocationAccessException)
                        } || (it - value.time).millis > config.geolocationUpdatePeriod) {
                        updateGeolocation()
                    }
                }
        }

        // Listen to the changes in runtime permissions and
        // update geolocation after those have been granted.
        launch {
            val localBroadcastManager = LocalBroadcastManager.getInstance(context)
            produce(
                localBroadcastManager = localBroadcastManager,
                intentFilterFactory = {
                    addAction(ACTION_PERMISSIONS_CHANGED)
                }
            ).consumeEach {
                val failedPrevTime = value.unbox().isRight()
                if (failedPrevTime) {
                    updateGeolocation()
                }
            }
        }

        updateGeolocation()
    }

    /**
     * Starts a job of retrieving last geolocation and
     * posting it.
     */
    private fun updateGeolocation() {
        pushWithDebounce(this, factory = { getGeolocation() })
    }

    @SuppressLint("MissingPermission")
    @WorkerThread
    private fun getGeolocation(): Moment<Either<Throwable, Geolocation>> {
        val either = try {
            fusedLocationClient.lastLocation.await()
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
                Either.left(it)
            }
        }

        return Moment.now(either)
    }

}
