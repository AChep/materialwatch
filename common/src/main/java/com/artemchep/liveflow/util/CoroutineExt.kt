package com.artemchep.liveflow.util

import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.suspendCancellableCoroutine

internal fun <T, O : Any> flowWithLifecycle(
    onActive: suspend (SendChannel<T>) -> O,
    onInactive: suspend (SendChannel<T>, O) -> Unit
): Flow<T> = channelFlow() {
    lateinit var observer: O
    try {
        observer = onActive(channel)
        awaitCancel()
    } finally {
        onInactive(channel, observer)
    }
}

internal suspend fun awaitCancel() = suspendCancellableCoroutine<Unit> { }
