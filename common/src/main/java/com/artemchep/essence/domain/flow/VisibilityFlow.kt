package com.artemchep.essence.domain.flow

import com.artemchep.essence.domain.models.AmbientMode
import com.artemchep.essence.domain.models.Visibility
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

fun VisibilityFlow(
    ambientModeFlow: Flow<AmbientMode>
) = flow {
    ambientModeFlow.collect {
        val visibility = it.toVisibility()
        emit(visibility)
    }
}

private fun AmbientMode.toVisibility() =
    Visibility(
        isTopStartVisible = !isOn,
        isTopEndVisible = !isOn,
        isBottomStartVisible = !isOn,
        isBottomEndVisible = !isOn
    )
