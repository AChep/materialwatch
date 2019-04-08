package com.artemchep.essence.domain.models

/**
 * @author Artem Chepurnoy
 */
data class Visibility(
    var isTopStartVisible: Boolean = true,
    var isTopEndVisible: Boolean = true,
    var isBottomStartVisible: Boolean = true,
    var isBottomEndVisible: Boolean = true
)
