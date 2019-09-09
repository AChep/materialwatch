package com.artemchep.essence.domain.flow

import android.content.Context
import com.artemchep.essence.ACTION_PERMISSIONS_CHANGED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun Context.flowOfPermissionChangedEvent(): Flow<Unit> =
    flowOfLocalIntent {
        addAction(ACTION_PERMISSIONS_CHANGED)
    }
        .map { Unit }
