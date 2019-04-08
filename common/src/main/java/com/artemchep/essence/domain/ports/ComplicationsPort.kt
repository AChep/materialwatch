package com.artemchep.essence.domain.ports

import android.content.Context
import android.util.SparseArray
import com.artemchep.essence.domain.models.Complication
import com.artemchep.essence.domain.models.Time
import kotlinx.coroutines.channels.ConflatedBroadcastChannel

/**
 * @author Artem Chepurnoy
 */
interface ComplicationsPort {
    val complicationsBroadcast: ConflatedBroadcastChannel<SparseArray<out (Context, Time) -> Complication>>
}
