package com.artemchep.essence.extensions

import android.content.Context
import android.support.wearable.complications.ComplicationData
import com.artemchep.essence.domain.models.Time

fun ComplicationData.isActive(time: Time): Boolean {
    val now = Time().millis
    return isActive(now)
}

/**
 * Refreshes the message of this complication according to
 * the time change.
 */
fun ComplicationData.getLongMessage(context: Context, time: Time): CharSequence? {
    val now = Time().millis
    val longText = longText?.getText(context, now)
    val longTitle = longTitle?.getText(context, now)
    return joinTitleAndText(longTitle, longText)
}

/**
 * Refreshes the message of this complication according to
 * the time change.
 */
fun ComplicationData.getShortMessage(context: Context, time: Time): CharSequence? {
    val now = Time().millis
    val shortText = shortText?.getText(context, now)
    val shortTitle = shortTitle?.getText(context, now)
    return joinTitleAndText(shortTitle, shortText)
}

private fun joinTitleAndText(title: CharSequence?, text: CharSequence?): CharSequence? {
    return if (text != null && title != null) {
        // Join text with title
        "$text $title"
    } else text ?: title
}
