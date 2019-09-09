package com.artemchep.essence.flow

import com.artemchep.essence.domain.models.Time
import com.artemchep.essence.domain.models.currentTime
import com.artemchep.liveflow.impl.MutableLiveFlowImpl

/**
 * @author Artem Chepurnoy
 */
class ManualTimeFlow : MutableLiveFlowImpl<Time>() {
    init {
        emitCurrentTime()
    }

    fun emitCurrentTime() = emit(currentTime)
}
