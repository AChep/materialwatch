package com.artemchep.liveflow.impl

import arrow.core.Option
import com.artemchep.liveflow.MutableLiveFlowStore

/**
 * Use this if you want to send an event to the
 * view.
 */
class MutableLiveFlowStoreNonPersistent<T> : MutableLiveFlowStore<T> {
    private val result: Option<T> = Option.empty()

    override fun set(value: T) = Unit

    override fun get(): Option<T> = result
}
