package com.artemchep.essence.domain.live.base

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 * @author Artem Chepurnoy
 */
abstract class BaseLiveData<T> : LiveData<T>(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var job: Job

    override fun onActive() {
        super.onActive()
        job = Job()
    }

    override fun onInactive() {
        job.cancel()
        super.onInactive()
    }

}