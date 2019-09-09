package com.artemchep.essence.domain.flow

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.artemchep.liveflow.util.flowWithLifecycle
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

private fun flowOfIntent(
    intentFilterBuilder: IntentFilter.() -> Unit,
    observe: (BroadcastReceiver, IntentFilter) -> Unit,
    removeObserver: (BroadcastReceiver) -> Unit
): Flow<Intent> {
    var observer: BroadcastReceiver? = null
    return flowWithLifecycle(
        onActive = {
            observer = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    GlobalScope.launch {
                        it.send(intent)
                    }
                }
            }.also { observer ->
                val intentFilter = IntentFilter().apply(intentFilterBuilder)
                observe(observer, intentFilter)
            }
        },
        onInactive = {
            observer?.let(removeObserver)
            observer = null
        }
    )
}

fun Context.flowOfSystemIntent(intentFilterBuilder: IntentFilter.() -> Unit) =
    flowOfIntent(
        intentFilterBuilder = intentFilterBuilder,
        observe = { receiver, intentFilter ->
            registerReceiver(receiver, intentFilter)
        },
        removeObserver = ::unregisterReceiver
    )

fun Context.flowOfLocalIntent(intentFilterBuilder: IntentFilter.() -> Unit) = run {
    val localBroadcastManager = LocalBroadcastManager.getInstance(this)
    flowOfIntent(
        intentFilterBuilder = intentFilterBuilder,
        observe = { receiver, intentFilter ->
            localBroadcastManager.registerReceiver(receiver, intentFilter)
        },
        removeObserver = localBroadcastManager::unregisterReceiver
    )
}
