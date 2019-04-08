package com.artemchep.essence.adapters

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.artemchep.essence.domain.models.Time
import com.artemchep.essence.domain.ports.EssentialsPort
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel

/**
 * @author Artem Chepurnoy
 */
class EssentialsPortImpl(
    private val context: Context
) : EssentialsPort {

    companion object {
        private const val AMBIENT_MODE_PERIOD = 3500L
    }

    override val timeBroadcast: ConflatedBroadcastChannel<Time> =
        ConflatedBroadcastChannel(EssentialsPort.DEFAULT_TIME.let(::Time))

    override val ambientModeBroadcast: ConflatedBroadcastChannel<Boolean> =
        ConflatedBroadcastChannel(EssentialsPort.DEFAULT_AMBIENT)

    fun CoroutineScope.setup() {
        setupTime()
        setupAmbientMode()
    }

    private fun CoroutineScope.setupTime() {
        fun updateTime() {
            launch {
                val now = System.currentTimeMillis().let(::Time)
                timeBroadcast.send(now)
            }
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

    private fun CoroutineScope.setupAmbientMode() {
        launch {
            var inAmbientMode = true

            while (isActive) {
                inAmbientMode = !inAmbientMode
                ambientModeBroadcast.send(inAmbientMode)

                // Wait a few seconds before toggling the
                // ambient mode again.
                delay(AMBIENT_MODE_PERIOD)
            }
        }
    }

}