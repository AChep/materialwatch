package com.artemchep.essence.extensions

infix fun Int.contains(v: Int): Boolean = (this and v) == v
