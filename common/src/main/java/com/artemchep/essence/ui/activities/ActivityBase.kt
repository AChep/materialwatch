package com.artemchep.essence.ui.activities

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.artemchep.essence.messageChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * @author Artem Chepurnoy
 */
abstract class ActivityBase : AppCompatActivity(), CoroutineScope {

    lateinit var job: Job

    lateinit var messageJob: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onStart() {
        super.onStart()
        job = Job()
    }

    override fun onResume() {
        super.onResume()
        messageJob = observeMessageChannel()
    }

    override fun onPause() {
        messageJob.cancel()
        super.onPause()
    }

    /**
     * Observes [messageChannel] and sends toasts
     * from that channel.
     */
    private fun CoroutineScope.observeMessageChannel(): Job {
        return launch {
            messageChannel.consumeEach {
                Toast.makeText(this@ActivityBase, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStop() {
        job.cancel()
        super.onStop()
    }

}
