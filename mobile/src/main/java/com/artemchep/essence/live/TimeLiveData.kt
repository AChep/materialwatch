package com.artemchep.essence.live

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.artemchep.essence.domain.live.base.BaseLiveData
import com.artemchep.essence.domain.models.Time
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * @author Artem Chepurnoy
 */
class TimeLiveData(
    private val context: Context
) : BaseLiveData<Time>() {

    init {
        value = Time()
    }

    override fun onActive() {
        super.onActive()

        fun updateTime() {
            val now = System.currentTimeMillis().let(::Time)
            postValue(now)
        }

        updateTime()

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
    }
}