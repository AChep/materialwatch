package com.artemchep.essence.domain.live

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import arrow.core.Either
import arrow.core.toOption
import com.artemchep.essence.ACTION_PERMISSIONS_CHANGED
import com.artemchep.essence.Cfg
import com.artemchep.essence.domain.exceptions.GeolocationAccessException
import com.artemchep.essence.domain.exceptions.GeolocationEmptyException
import com.artemchep.essence.domain.live.base.BaseLiveData
import com.artemchep.essence.domain.models.Geolocation
import com.artemchep.essence.domain.models.Moment
import com.artemchep.essence.domain.ports.EssentialsPort
import com.artemchep.essence.extensions.await
import com.artemchep.essence.extensions.produce
import com.artemchep.essence.extensions.receive
import com.artemchep.essence.ifDebug
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
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
    private val essentialsPort: EssentialsPort
) : BaseLiveData<Moment<Either<Throwable, Geolocation>>>() {

    companion object {
        private const val TAG = "GeolocationLiveData"
    }

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private var geolocationJob: Job? = null

    override fun onActive() {
        super.onActive()
        consumeEach(essentialsPort.timeBroadcast) {
            val prevTime = value?.time
            if (prevTime == null || (it - prevTime).millis > config.geolocationUpdatePeriod) {
                updateGeolocation()
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
                val failedPrevTime = receive().value.isRight()
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
        if (geolocationJob?.isActive != true) {
            ifDebug {
                Log.d(TAG, "Launching an update job.")
            }

            geolocationJob = launch(Dispatchers.IO) {
                val value = getGeolocation()
                postValue(value)
            }
        } else ifDebug {
            Log.d(TAG, "Ignoring an update job.")
        }
    }

    @SuppressLint("MissingPermission")
    @Throws(GeolocationAccessException::class)
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
