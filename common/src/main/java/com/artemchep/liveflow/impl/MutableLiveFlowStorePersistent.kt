package com.artemchep.liveflow.impl

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import com.artemchep.liveflow.MutableLiveFlowStore

/**
 * Use this if you want to send new data to the
 * view.
 */
class MutableLiveFlowStorePersistent<T> : MutableLiveFlowStore<T> {
    @Volatile
    private var result: Option<T> = None

    override fun set(value: T) {
        result = Some(value)
    }

    override fun get(): Option<T> = result
}
