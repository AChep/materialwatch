package com.artemchep.essence.live

import android.content.Context
import android.text.format.DateFormat
import android.util.SparseArray
import androidx.core.util.set
import com.artemchep.essence.R
import com.artemchep.essence.WATCH_COMPLICATION_THIRD
import com.artemchep.essence.domain.live.base.Live3
import com.artemchep.essence.domain.models.Complication
import com.artemchep.essence.domain.models.Time
import java.util.*

/**
 * @author Artem Chepurnoy
 */
class ComplicationsRawLiveData :
    Live3<SparseArray<out (Context, Time) -> Complication>>(SparseArray()) {

    init {
        val sparse = SparseArray<(Context, Time) -> Complication>()

        // Show a date as third
        // complication
        sparse[WATCH_COMPLICATION_THIRD] = { context, time ->
            val icon = context.getDrawable(R.drawable.ic_today)

            val dateFormat = DateFormat.getMediumDateFormat(context)
            val dateStr = dateFormat.format(Date())
            Complication(
                normalIconDrawable = icon,
                shortMsg = dateStr,
                isActive = true
            )
        }

        value = sparse
    }

}