package com.artemchep.essence.domain.models

import android.graphics.drawable.Drawable

/**
 * @author Artem Chepurnoy
 */
data class Complication(
    var normalIconDrawable: Drawable? = null,
    var ambientIconDrawable: Drawable? = null,
    var longMsg: CharSequence? = null,
    var shortMsg: CharSequence? = null,
    var isActive: Boolean = false
)
