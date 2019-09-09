package com.artemchep.liveflow

import kotlinx.coroutines.flow.Flow

/**
 * @author Artem Chepurnoy
 */
interface LiveFlow<T> {
    fun share(): Flow<T>
}
