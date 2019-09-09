package com.artemchep.essence.domain.models

class Time(val millis: Long) {
    /**
     * Create an instance of
     * current time.
     */
    constructor() : this(millis = System.currentTimeMillis())

    operator fun minus(time: Time): Time =
        this - time.millis

    operator fun minus(time: Long): Time =
        Time(millis - time)
}

val currentTime: Time
    get() = Time()
