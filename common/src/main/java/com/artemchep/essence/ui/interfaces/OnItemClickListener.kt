package com.artemchep.essence.ui.interfaces

import android.view.View

/**
 * @author Artem Chepurnoy
 */
interface OnItemClickListener<in T> {

    fun onItemClick(view: View, model: T)

}