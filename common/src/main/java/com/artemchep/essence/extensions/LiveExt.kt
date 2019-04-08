package com.artemchep.essence.extensions

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlin.coroutines.resume

@MainThread
suspend fun <T> LiveData<T>.receive(): T {
    return suspendCancellableCoroutine<T> { continuation ->
        val observer = object : Observer<T> {
            override fun onChanged(t: T) {
                continuation.resume(t)

                // Unregister the subscriber after we received
                // the data.
                removeObserver(this)
            }
        }

        observeForever(observer)

        // Remove the observer on
        // job's cancellation.
        continuation.invokeOnCancellation {
            GlobalScope.launch(Dispatchers.Main) { removeObserver(observer) }
        }
    }
}

/**
 * Produces a channel from given
 * live data.
 */
fun <T> CoroutineScope.produceFromLive(liveData: LiveData<T>): Channel<T> {
    val channel = Channel<T>(Channel.RENDEZVOUS)
    val observer = Observer<T> { t ->
        runBlocking {
            channel.send(t)
        }
    }

    launch(Dispatchers.Main) {
        suspendCancellableCoroutine<Unit> { continuation ->
            liveData.observeForever(observer)

            // Remove the observer on
            // job's cancellation.
            continuation.invokeOnCancellation {
                liveData.removeObserver(observer)
            }
        }
    }

    channel.invokeOnClose {
        liveData.removeObserver(observer)
    }

    return channel
}
