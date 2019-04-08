package com.artemchep.essence.extensions

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks

fun <T> Task<T>.await(): T = Tasks.await(this)
