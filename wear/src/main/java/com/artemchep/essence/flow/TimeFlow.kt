package com.artemchep.essence.flow

import com.artemchep.essence.domain.models.Time
import com.artemchep.essence.domain.models.currentTime
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * @author Artem Chepurnoy
 */
@Suppress("FunctionName")
fun ManualTimeFlow() = MutableStateFlow(currentTime)

/**
 * Updates the value of the time flow to
 * current time.
 */
fun MutableStateFlow<Time>.emitCurrentTime() {
    value = currentTime
}
