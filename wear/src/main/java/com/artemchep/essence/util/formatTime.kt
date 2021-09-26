package com.artemchep.essence.util

import android.content.Context
import java.text.DateFormat
import java.util.*

fun formatTime(time: Int, format: DateFormat): String = Calendar.getInstance()
    .apply {
        val h = time / 60
        val m = time % 60
        set(Calendar.HOUR_OF_DAY, h)
        set(Calendar.MINUTE, m)
    }.time.let(format::format)

fun createTimeFormat(context: Context): DateFormat =
    android.text.format.DateFormat.getTimeFormat(context)