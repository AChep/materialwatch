package com.artemchep.essence.util

import android.content.Context
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

fun formatDate(format: DateFormat): String = Calendar.getInstance()
    .apply {
    }.time.let(format::format)

fun createDateFormat(context: Context): DateFormat =
    SimpleDateFormat("EEE, MMM d")