package com.artemchep.liveflow

/**
 * @author Artem Chepurnoy
 */
interface MutableLiveFlowStore<T> : LiveFlowStore<T> {
    fun set(value: T)
}
