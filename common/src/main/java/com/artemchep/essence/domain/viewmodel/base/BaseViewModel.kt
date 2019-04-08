package com.artemchep.essence.domain.viewmodel.base

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * @author Artem Chepurnoy
 */
abstract class BaseViewModel(application: Application) :
    AndroidViewModel(application),
    CoroutineScope {

    private val job = SupervisorJob()

    val context: Context
        get() = getApplication()

    override val coroutineContext = Dispatchers.Main + job

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }

}