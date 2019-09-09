package com.artemchep.liveflow.util

internal inline fun <R> sync(lock: Any, block: () -> R): R = synchronized(lock, block)
