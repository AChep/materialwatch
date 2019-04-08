package com.artemchep.essence.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import com.artemchep.essence_common.R

/**
 * @author Artem Chepurnoy
 */
class BrandTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    init {
        typeface = ResourcesCompat.getFont(context, R.font.manrope_semibold)
    }

}