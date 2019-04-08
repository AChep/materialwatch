package com.artemchep.essence.domain.models

/**
 * @author Artem Chepurnoy
 */
sealed class Screen<T>

class LoadingScreen<T> : Screen<T>()
class FailureScreen<T> : Screen<T>()
class OkScreen<T>(
    val data: T
) : Screen<T>()
