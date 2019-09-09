package com.artemchep.liveflow.impl

import arrow.core.Option
import com.artemchep.liveflow.MutableLiveFlowStore

/**
 * Use this if you want to send new data to the
 * view.
 */
class MutableLiveFlowStorePersistent<T> : MutableLiveFlowStore<T> {
    @Volatile
    private var result: Option<T> = Option.empty()

    override fun set(value: T) {
        result = Option.just(value)
    }

    override fun get(): Option<T> = result
}
