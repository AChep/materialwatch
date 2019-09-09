package com.artemchep.liveflow.impl

import com.artemchep.liveflow.LiveFlow
import com.artemchep.liveflow.LiveFlowStore
import com.artemchep.liveflow.get
import com.artemchep.liveflow.util.flowWithLifecycle
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * @author Artem Chepurnoy
 */
open class LiveFlowImpl<T>(
    val store: LiveFlowStore<T> = MutableLiveFlowStorePersistent()
) : LiveFlow<T> {

    /**
     * Returns last value that has been emitted to
     * consumers or `null`.
     */
    val value: T? = store.get().orNull()

    protected val mutex: Mutex = Mutex()

    protected var consumers: List<SendChannel<T>> = emptyList()

    private suspend fun setConsumers(new: List<SendChannel<T>>) {
        val newSize = new.size
        val oldSize = consumers.size

        consumers = new

        when {
            oldSize == 0 && newSize > 0 -> onActive()
            newSize == 0 && oldSize > 0 -> onInactive()
        }

        when {
            // notify a newly added consumer
            newSize - oldSize == 1 -> {
                store.get { value ->
                    val channel = new.last()
                    channel.send(value)
                }
            }
        }
    }

    override fun share(): Flow<T> =
        flowWithLifecycle<T, Unit>(
            onActive = { channel ->
                // Add a channel to the list of
                // consumers.
                mutex.withLock {
                    setConsumers(consumers + channel)
                }
                Unit
            },
            onInactive = { channel, _ ->
                // Remove a channel from the list of
                // consumers.
                mutex.withLock {
                    setConsumers(consumers - channel)
                }
            }
        )

    open fun onActive() {
    }

    open fun onInactive() {
    }
}
