package com.artemchep.liveflow.impl

import com.artemchep.liveflow.MutableLiveFlow
import com.artemchep.liveflow.MutableLiveFlowStore
import com.artemchep.liveflow.util.sync
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext

/**
 * @author Artem Chepurnoy
 */
open class MutableLiveFlowImpl<T>(
    protected val mutableStore: MutableLiveFlowStore<T> = MutableLiveFlowStorePersistent()
) : LiveFlowImpl<T>(mutableStore), MutableLiveFlow<T> {

    private var scopeJob: Job = Job().apply { complete() }

    /** Coroutine scope of the live flow. */
    protected val scope: CoroutineScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = Dispatchers.Main + scopeJob
    }

    private val queue: MutableList<T> = ArrayList()

    override fun onActive() {
        super.onActive()
        scopeJob = Job()
    }

    override fun onInactive() {
        scopeJob.cancel()
        super.onInactive()
    }

    /**
     * Emits the value to all observers and
     * remembers it.
     */
    override fun emit(value: T) {
        sync(queue) {
            queue.add(value)
        }

        GlobalScope.launch {
            mutex.withLock {
                sync(queue) {
                    queue
                        .toList()
                        .apply {
                            queue.clear()
                        }
                }.forEach { value ->
                    mutableStore.set(value)

                    // Notify the consumers about this
                    // new object.
                    consumers.forEach { it.send(value) }
                }
            }
        }
    }

}

fun <T> Flow<T>.live(store: MutableLiveFlowStore<T> = MutableLiveFlowStorePersistent()) =
    object : MutableLiveFlowImpl<T>(store) {
        override fun onActive() {
            super.onActive()
            scope.launch {
                // Emits the flow to our live flow
                // source.
                collect {
                    emit(it)
                }
            }
        }
    }

fun <T> Flow<T>.shared(store: MutableLiveFlowStore<T> = MutableLiveFlowStorePersistent()) =
    live(store).share()

//fun <T, A, B> reduce(
//    a: MutableLiveFlowImpl<A>,
//    b: MutableLiveFlowImpl<B>,
//    reduce: MutableLiveFlow<T>.(Option<A>, Option<B>) -> Unit
//): MutableLiveFlow<T> {
//    val aFlow = a.share()
//    val bFlow = b.share()
//
//    return object : MutableLiveFlowImpl<T>(LiveFlowStore.Persistent()) {
//        override fun onActive() {
//            super.onActive()
//            fun internalReduce(
//                aValue: Option<A> = a.store.get(),
//                bValue: Option<B> = b.store.get()
//            ) {
//                reduce(aValue, bValue)
//            }
//
//            scope.launch {
//                aFlow.collect { internalReduce(aValue = it.toOption()) }
//            }
//            scope.launch {
//                bFlow.collect { internalReduce(bValue = it.toOption()) }
//            }
//        }
//    }
//}
