package com.artemchep.liveflow

/**
 * @author Artem Chepurnoy
 */
interface MutableLiveFlow<T> : LiveFlow<T> {
    fun emit(value: T)
}

fun <T, Object : MutableLiveFlow<T>> Object.withEmitted(value: T) = apply { emit(value) }
