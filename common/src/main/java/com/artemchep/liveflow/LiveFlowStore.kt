package com.artemchep.liveflow

import arrow.core.Option

/**
 * @author Artem Chepurnoy
 */
interface LiveFlowStore<T> {
    fun get(): Option<T>
}

inline fun <T> LiveFlowStore<T>.get(block: (T) -> Unit) {
    get().fold(
        ifEmpty = {},
        ifSome = block
    )
}
