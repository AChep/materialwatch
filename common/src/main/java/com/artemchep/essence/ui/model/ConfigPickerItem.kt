package com.artemchep.essence.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @author Artem Chepurnoy
 */
@Parcelize
data class ConfigPickerItem(
    val key: String,
    val color: Int,
    val title: String
) : Parcelable
