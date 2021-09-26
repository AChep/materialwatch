package com.artemchep.essence.flow

import com.artemchep.essence.domain.models.currentTime
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

@Suppress("FunctionName")
fun PreviewTimeFlow() =
    flow {
        coroutineScope {
            while (isActive) {
                emit(currentTime)
                delay(100L)
            }
        }
    }