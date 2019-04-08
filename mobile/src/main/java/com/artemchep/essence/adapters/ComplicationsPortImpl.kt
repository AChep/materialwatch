package com.artemchep.essence.adapters

import android.content.Context
import android.text.format.DateFormat
import android.util.SparseArray
import androidx.core.util.set
import com.artemchep.essence.R
import com.artemchep.essence.WATCH_COMPLICATION_THIRD
import com.artemchep.essence.domain.models.Complication
import com.artemchep.essence.domain.models.Time
import com.artemchep.essence.domain.ports.ComplicationsPort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.launch
import java.util.*

/**
 * @author Artem Chepurnoy
 */
class ComplicationsPortImpl(
    private val context: Context
) : ComplicationsPort {

    override val complicationsBroadcast: ConflatedBroadcastChannel<SparseArray<out (Context, Time) -> Complication>> =
        ConflatedBroadcastChannel(SparseArray())

    fun CoroutineScope.setup() {
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

        launch {
            complicationsBroadcast.send(sparse)
        }
    }

}