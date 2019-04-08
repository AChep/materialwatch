package com.artemchep.essence.domain.models

/**
 * @author Artem Chepurnoy
 */
data class Moment<T>(
    val time: Time,
    val value: T
) {
    companion object {
        fun <T> now(value: T) = Moment(Time(), value)
    }
}
