package com.artemchep.essence.extensions

import androidx.lifecycle.LifecycleOwner
import com.artemchep.essence.domain.lifecycle.withLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

fun <Scope, T> Flow<T>.injectObserver(
    scope: Scope,
    collector: suspend (T) -> Unit
) where Scope : LifecycleOwner, Scope : CoroutineScope {
    var job: Job? = null

    withLifecycle(
        scope,
        makeActive = {
            job = scope.launch(Dispatchers.Main) {
                collect(collector)
            }
        },
        makeInactive = {
            job?.cancel()
            job = null
        }
    )
}
