package com.artemchep.essence.domain.ports

import com.artemchep.essence.domain.models.Time
import kotlinx.coroutines.channels.ConflatedBroadcastChannel

/**
 * @author Artem Chepurnoy
 */
interface EssentialsPort {
    val timeBroadcast: ConflatedBroadcastChannel<Time>
    val ambientModeBroadcast: ConflatedBroadcastChannel<Boolean>

    companion object {
        const val DEFAULT_TIME = 0L
        const val DEFAULT_AMBIENT = false
    }
}