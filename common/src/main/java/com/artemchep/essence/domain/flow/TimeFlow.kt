package com.artemchep.essence.domain.flow

import android.content.Context
import android.content.Intent
import com.artemchep.essence.domain.models.Time
import com.artemchep.essence.domain.models.currentTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

fun Context.flowOfTime(onStartEmitCurrentTime: Boolean = true): Flow<Time> =
    flowOfSystemIntent {
        addAction(Intent.ACTION_TIMEZONE_CHANGED)
        addAction(Intent.ACTION_TIME_CHANGED)
        addAction(Intent.ACTION_TIME_TICK)
    }
        .map { currentTime }
        .onStart {
            if (onStartEmitCurrentTime) emit(currentTime)
        }
