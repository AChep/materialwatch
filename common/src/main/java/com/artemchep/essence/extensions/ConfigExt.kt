package com.artemchep.essence.extensions

import com.artemchep.config.Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

fun CoroutineScope.launchObserver(config: Config<String>, block: (Set<String>) -> Unit) {
    launch {
        val observer = object : Config.OnConfigChangedListener<String> {
            override fun onConfigChanged(keys: Set<String>) {
                block(keys)
            }
        }

        suspendCancellableCoroutine<Unit> {
            config.observe(observer)

            it.invokeOnCancellation {
                config.removeObserver(observer)
            }
        }
    }
}
