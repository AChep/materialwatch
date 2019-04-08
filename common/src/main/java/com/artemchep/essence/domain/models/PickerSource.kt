package com.artemchep.essence.domain.models

import com.artemchep.essence.ui.model.ConfigPickerItem

/**
 * @author Artem Chepurnoy
 */
data class PickerSource(
    val title: String,
    val selectedKey: String,
    val items: List<ConfigPickerItem>,
    val requestCode: Int
)
