package com.artemchep.essence.domain.models

/**
 * @author Artem Chepurnoy
 */
class Event<T>(
    private var data: T?
) {
    /**
     * Returns an event if it exists,
     * otherwise returns `null`.
     */
    fun consume(): T? = data
        ?.also {
            data = null
        }
}
