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
): Flow<Intent> =
    flowWithLifecycle<Intent, BroadcastReceiver>(
        onActive = {
            object : BroadcastReceiver() {
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
        onInactive = { _, observer ->
            removeObserver(observer)
        }
    )

fun Context.flowOfSystemIntent(intentFilterBuilder: IntentFilter.() -> Unit) =
    flowOfIntent(
        intentFilterBuilder = intentFilterBuilder,
        observe = { receiver, intentFilter ->
            applicationContext.registerReceiver(receiver, intentFilter)
        },
        removeObserver = {
            applicationContext.unregisterReceiver(it)
        }
    )

fun Context.flowOfLocalIntent(intentFilterBuilder: IntentFilter.() -> Unit) = run {
    val localBroadcastManager = LocalBroadcastManager.getInstance(applicationContext)
    flowOfIntent(
        intentFilterBuilder = intentFilterBuilder,
        observe = { receiver, intentFilter ->
            localBroadcastManager.registerReceiver(receiver, intentFilter)
        },
        removeObserver = { receiver ->
            localBroadcastManager.unregisterReceiver(receiver)
        }
    )
}
