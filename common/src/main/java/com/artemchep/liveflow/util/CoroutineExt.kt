package com.artemchep.liveflow.util

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

internal fun <T, O : Any> flowWithLifecycle(
    onActive: suspend (SendChannel<T>) -> O,
    onInactive: suspend (SendChannel<T>, O) -> Unit
): Flow<T> = flow {
    coroutineScope {
        val channel = Channel<T>(Channel.RENDEZVOUS)
        // Control the lifecycle of this flow
        // collector.
        launch {
            lateinit var observer: O
            try {
                observer = onActive(channel)
                awaitCancel()
            } finally {
                onInactive(channel, observer)
            }
        }

        channel.consumeEach { emit(it) }
    }
}

internal suspend fun awaitCancel() = suspendCancellableCoroutine<Unit> { }
