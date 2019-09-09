package com.artemchep.essence.domain.flow

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.SparseArray
import androidx.core.util.forEach
import com.artemchep.essence.domain.models.AmbientMode
import com.artemchep.essence.domain.models.Complication
import com.artemchep.essence.domain.models.Time
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@UseExperimental(ExperimentalCoroutinesApi::class)
fun ComplicationFlow(
    context: Context,
    ambientModeFlow: Flow<AmbientMode>,
    complicationsFactoryFlow: Flow<SparseArray<out (Context, Time) -> Complication>>,
    timeFlow: Flow<Time>
): Flow<Map<Int, Pair<Drawable?, String?>>> =
    timeFlow
        .combine(ambientModeFlow) { time, ambientMode ->
            time to ambientMode
        }
        .combine(complicationsFactoryFlow) { (time, ambientMode), sparse ->
            // Form a map of new complications for current
            // conditions.
            val map = HashMap<Int, Pair<Drawable?, String?>>()
            sparse.forEach { watchFaceComplicationId, complication ->
                val model = complication.invoke(context, time)
                if (model.isActive) {
                    val text = model.longMsg ?: model.shortMsg ?: return@forEach // skip if null
                    val icon = model.ambientIconDrawable
                        ?.takeIf {
                            ambientMode.isOn
                        }
                        ?: model.normalIconDrawable

                    map[watchFaceComplicationId] = icon to text.toString()
                }
            }

            map
        }
