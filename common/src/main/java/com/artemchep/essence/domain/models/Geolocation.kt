package com.artemchep.essence.domain.models

/** Radius of the Earth in meters */
private const val EARTH_RADIUS = 6371000f

/**
 * @author Artem Chepurnoy
 */
data class Geolocation(
    val latitude: Double,
    val longitude: Double
)
