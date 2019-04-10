package com.artemchep.essence.domain.models

/**
 * @author Artem Chepurnoy
 */
data class RuntimePermissionsRequest(
    val permissions: List<String>,
    val requestCode: Int
)
