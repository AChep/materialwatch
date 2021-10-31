package com.artemchep.essence.ui.model

import android.graphics.drawable.Drawable

/**
 * @author Artem Chepurnoy
 */
data class ConfigItem(
    val id: Int,
    var icon: Drawable? = null,
    val title: String,
    var summary: String? = null,
    var checked: Boolean? = null,
    var button: Drawable? = null,
)
