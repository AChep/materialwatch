package com.artemchep.essence.domain.live.base

import androidx.lifecycle.LifecycleOwner
import com.artemchep.essence.domain.DEFAULT_DEBOUNCE
import com.artemchep.essence.domain.lifecycle.withLifecycle
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlin.coroutines.CoroutineContext

/**
 * @author Artem Chepurnoy
 */
open class Live3<T : Any>(
    var value: T
) : CoroutineScope {

    // Coroutine scope

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var job: Job

    // Main

    private var observers: MutableList<(T) -> Unit> = ArrayList()

    private val monitor = Any()

    /**
     * `true` if the creature is active right now,
     * `false` otherwise.
     */
    var isActive = false
        set(value) {
            field = value

            if (value) {
                onActive()
            } else onInactive()
        }

    private var pushWithDebounceJob: Job? = null

    fun openSubscription(scope: CoroutineScope): ReceiveChannel<T> {
        val channel = Channel<T>(Channel.RENDEZVOUS)
        val observer: (T) -> Unit = { model ->
            scope.launch {
                channel.send(model)
            }
        }

        // Subscribe and unsubscribe
        // on completion
        val job = scope.launch {
            observe(observer)
            suspendCancellableCoroutine<Unit> { }
        }
        job.invokeOnCompletion {
            GlobalScope.launch {
                removeObserver(observer)
            }
        }

        return channel
    }

    fun observe(observer: (T) -> Unit) {
        synchronized(monitor) {
            observers.add(observer)

            // Immediately notify
            // the observer.
            observer.invoke(value)
        }

        synchronized(monitor) {
            performActiveStateChange()
        }
    }

    fun removeObserver(observer: (T) -> Unit) {
        synchronized(monitor) {
            observers.remove(observer)
            performActiveStateChange()
        }
    }

    private fun performActiveStateChange(shouldBeActive: Boolean = shouldBeActive()) {
        if (isActive != shouldBeActive) isActive = shouldBeActive
    }

    /**
     * @return `true` if the creature should be active right now,
     * `false` if it should be inactive
     */
    private fun shouldBeActive() = observers.isNotEmpty()

    fun push(model: T, retain: Boolean = true) {
        pushWithDebounceJob?.cancel()
        pushWithDebounceJob = null

        synchronized<Unit>(monitor) {
            if (retain) {
                value = model
            }

            GlobalScope.launch(Dispatchers.Main.immediate) {
                observers.forEach {
                    it.invoke(model)
                }
            }
        }
    }

    fun pushWithDebounce(scope: CoroutineScope, factory: suspend () -> T, retain: Boolean = true) {
        pushWithDebounceJob?.cancel()
        pushWithDebounceJob = scope.launch(Dispatchers.IO) {
            delay(DEFAULT_DEBOUNCE)

            val model = factory()
            if (model != value) {
                push(model, retain)
            }
        }.apply {
            invokeOnCompletion {
                pushWithDebounceJob = null
            }
        }
    }

    open fun onActive() {
        job = Job()
    }

    open fun onInactive() {
        job.cancel()
    }

}

fun <T : Any> Flow<T>.live3(initialValue: T) = object : Live3<T>(initialValue) {
    override fun onActive() {
        super.onActive()
        launch {
            collect {
                value = it
            }
        }
    }
}

fun <T : Any> Live3<T>.injectObserver(owner: LifecycleOwner, observer: (T) -> Unit) {
    withLifecycle(
        owner,
        { observe(observer) },
        { removeObserver(observer) }
    )
}
