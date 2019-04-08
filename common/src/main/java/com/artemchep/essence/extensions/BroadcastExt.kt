package com.artemchep.essence.extensions

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

fun CoroutineScope.produce(
    localBroadcastManager: LocalBroadcastManager,
    intentFilterFactory: IntentFilter.() -> Unit
): Channel<Intent> {
    val channel = Channel<Intent>(Channel.RENDEZVOUS)

    val intentFilter = IntentFilter().also(intentFilterFactory)
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            launch {
                channel.send(intent)
            }
        }
    }

    launch {
        suspendCancellableCoroutine<Unit> {
            localBroadcastManager.registerReceiver(receiver, intentFilter)

            it.invokeOnCancellation {
                localBroadcastManager.unregisterReceiver(receiver)
            }
        }
    }

    channel.invokeOnClose {
        localBroadcastManager.unregisterReceiver(receiver)
    }

    return channel
}
