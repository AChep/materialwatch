package com.artemchep.essence.extensions

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.artemchep.essence.domain.live.base.BaseLiveData
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlin.coroutines.resume

fun <T> BroadcastChannel<T>.toLiveData(): LiveData<T> =
    object : BaseLiveData<T>() {
        override fun onActive() {
            super.onActive()
            launch {
                consumeEach(::postValue)
            }
        }
    }

suspend fun <T> LiveData<T>.receive(): T {
    return suspendCancellableCoroutine { continuation ->
        val observer = object : Observer<T> {
            override fun onChanged(t: T) {
                continuation.resume(t)

                // Unregister the subscriber after we received
                // the data.
                removeObserver(this)
            }
        }

        runBlocking {
            launch(Dispatchers.Main) { observeForever(observer) }
        }

        // Remove the observer on
        // job's cancellation.
        continuation.invokeOnCancellation {
            runBlocking {
                launch(Dispatchers.Main) { removeObserver(observer) }
            }
        }
    }
}

/**
 * Produces a channel from given
 * live data.
 */
fun <T> CoroutineScope.produce(liveData: LiveData<T>): Channel<T> {
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
