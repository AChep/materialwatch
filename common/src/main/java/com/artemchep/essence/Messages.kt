package com.artemchep.essence

import kotlinx.coroutines.channels.BroadcastChannel

val messageChannel = BroadcastChannel<String>(1)
