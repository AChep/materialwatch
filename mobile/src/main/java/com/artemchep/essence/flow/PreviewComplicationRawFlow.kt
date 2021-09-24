package com.artemchep.essence.flow

import android.content.Context
import android.text.format.DateFormat
import android.util.SparseArray
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.util.set
import com.artemchep.essence.R
import com.artemchep.essence.WATCH_COMPLICATION_THIRD
import com.artemchep.essence.domain.models.Complication
import com.artemchep.essence.domain.models.Time
import kotlinx.coroutines.flow.flow
import java.util.*

@Suppress("FunctionName")
fun PreviewComplicationRawFlow() =
    flow {
        val sparse = SparseArray<(Context, Time) -> Complication>()

        // Show a date as third
        // complication
        sparse[WATCH_COMPLICATION_THIRD] = { context, _ ->
            val icon = AppCompatResources.getDrawable(context, R.drawable.ic_today)

            val dateFormat = DateFormat.getMediumDateFormat(context)
            val dateStr = dateFormat.format(Date())
            Complication(
                normalIconDrawable = icon,
                shortMsg = dateStr,
                isActive = true
            )
        }

        emit(sparse)
    }