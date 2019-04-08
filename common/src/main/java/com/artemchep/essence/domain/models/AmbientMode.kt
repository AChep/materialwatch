package com.artemchep.essence.domain.models

/**
 * @author Artem Chepurnoy
 */
sealed class AmbientMode(
    val isOn: Boolean
) {
    object On : AmbientMode(true) {
        override fun toggle(): AmbientMode = Off
    }

    object Off : AmbientMode(false) {
        override fun toggle(): AmbientMode = On
    }

    abstract fun toggle(): AmbientMode
}

fun Boolean.asAmbientMode() = if (this) AmbientMode.On else AmbientMode.Off
