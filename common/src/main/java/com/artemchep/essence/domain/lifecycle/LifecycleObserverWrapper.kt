package com.artemchep.essence.domain.lifecycle

import androidx.lifecycle.GenericLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

/**
 * Lifecycle aware wrapper around the observer, this object controls
 * addition and removal of the [Live3]'s observer.
 */
private class LifecycleObserverWrapper(
    owner: LifecycleOwner,
    private val makeActive: () -> Unit,
    private val makeInactive: () -> Unit
) : GenericLifecycleObserver {

    private val lifecycle = owner.lifecycle

    private var isActive = false
        private set(value) {
            field = value
            onActiveStateChanged(value)
        }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (lifecycle.currentState) {
            Lifecycle.State.DESTROYED -> stopListening()
            else -> {
                // Do nothing, cause we don't need to
                // send the event on subscription.
            }
        }

        performActiveStateChange()
    }

    fun startListening() {
        lifecycle.addObserver(this)
        performActiveStateChange()
    }

    fun stopListening() {
        lifecycle.removeObserver(this)
        performActiveStateChange(false)
    }

    private fun performActiveStateChange(shouldBeActive: Boolean = shouldBeActive()) {
        if (isActive != shouldBeActive) isActive = shouldBeActive
    }

    private fun shouldBeActive() = lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)

    /**
     * Called when a state of a lifecycle has
     * changed.
     */
    private fun onActiveStateChanged(isActive: Boolean) {
        // Update the counter of active observers
        if (isActive) {
            makeActive()
        } else makeInactive()
    }

}

fun withLifecycle(owner: LifecycleOwner, makeActive: () -> Unit, makeInactive: () -> Unit) {
    if (owner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
        return
    }

    LifecycleObserverWrapper(owner, makeActive, makeInactive).startListening()
}