package com.artemchep.essence.live

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.artemchep.essence.domain.DEFAULT_DEBOUNCE
import com.artemchep.essence.domain.live.base.Live3
import com.artemchep.essence.domain.models.Time
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * @author Artem Chepurnoy
 */
class TimeLiveData(
    private val context: Context
) : Live3<Time>(Time()) {

    private var timeJob: Job? = null

    override fun onActive() {
        super.onActive()
        launch {
            val broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    updateTime()
                }
            }
            suspendCancellableCoroutine<Unit> {
                context.registerReceiver(broadcastReceiver,
                    IntentFilter().apply {
                        addAction(Intent.ACTION_TIMEZONE_CHANGED)
                        addAction(Intent.ACTION_TIME_CHANGED)
                        addAction(Intent.ACTION_TIME_TICK)
                    })

                it.invokeOnCancellation {
                    context.unregisterReceiver(broadcastReceiver)
                }
            }
        }

        updateTime()
    }

    private fun updateTime() {
        timeJob?.cancel()
        timeJob = launch {
            delay(DEFAULT_DEBOUNCE)

            val time = Time()
            push(time)
        }.apply {
            invokeOnCompletion {
                timeJob = null
            }
        }
    }

}