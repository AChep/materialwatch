package com.artemchep.essence.flow

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

private const val AMBIENT_MODE_PERIOD = 5000L

@Suppress("FunctionName")
fun PreviewAmbientModeFlow() =
    flow {
        var inAmbientMode = false

        coroutineScope {
            while (isActive) {
                inAmbientMode = inAmbientMode
                    .also {
                        emit(it)
                    }
                    .not()

                // Wait a few seconds before toggling the
                // ambient mode again.
                delay(AMBIENT_MODE_PERIOD)
            }
        }
    }