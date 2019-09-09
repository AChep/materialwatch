package com.artemchep.essence.flow

import com.artemchep.essence.domain.models.AmbientMode
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

private const val AMBIENT_MODE_PERIOD = 3500L

fun PreviewAmbientModeFlow() =
    flow {
        var ambientMode: AmbientMode = AmbientMode.On

        coroutineScope {
            while (isActive) {
                ambientMode = ambientMode.toggle()
                    .also {
                        emit(it)
                    }

                // Wait a few seconds before toggling the
                // ambient mode again.
                delay(AMBIENT_MODE_PERIOD)
            }
        }
    }