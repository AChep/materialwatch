package com.artemchep.liveflow.util

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

internal fun <T> flowWithLifecycle(
    onActive: suspend (SendChannel<T>) -> Unit,
    onInactive: suspend (SendChannel<T>) -> Unit
): Flow<T> = flow {
    coroutineScope {
        val channel = Channel<T>(Channel.RENDEZVOUS)
        // Control the lifecycle of this flow
        // collector.
        launch {
            try {
                onActive(channel)
                awaitCancel()
            } finally {
                onInactive(channel)
            }
        }

        channel.consumeEach { emit(it) }
    }
}

internal suspend fun awaitCancel() = suspendCancellableCoroutine<Unit> { }
