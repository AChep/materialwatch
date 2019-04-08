package com.artemchep.essence

import com.artemchep.essence_common.BuildConfig

inline fun ifDebug(crossinline block: () -> Unit) {
    if (BuildConfig.DEBUG) {
        block()
    }
}