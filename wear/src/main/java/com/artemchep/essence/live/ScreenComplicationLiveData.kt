package com.artemchep.essence.live

import android.content.ComponentName
import android.content.Context
import android.support.wearable.complications.ComplicationProviderInfo
import android.support.wearable.complications.ProviderInfoRetriever
import android.util.SparseBooleanArray
import com.artemchep.essence.*
import com.artemchep.essence.domain.live.base.BaseLiveData
import com.artemchep.essence.domain.models.FailureScreen
import com.artemchep.essence.domain.models.LoadingScreen
import com.artemchep.essence.domain.models.OkScreen
import com.artemchep.essence.domain.models.Screen
import com.artemchep.essence.service.WatchFaceService
import com.artemchep.essence.ui.model.ConfigItem
import com.artemchep.mw.R
import kotlinx.coroutines.isActive
import java.util.concurrent.Executors

private typealias Data = Pair<Int, ComplicationProviderInfo?>

/**
 * @author Artem Chepurnoy
 */
class ScreenComplicationsLiveData(
    private val context: Context
) : BaseLiveData<Screen<List<ConfigItem>>>() {

    private lateinit var providerInfoRetriever: ProviderInfoRetriever

    private val emptyModelIcon = context.getDrawable(R.drawable.ic_plus)

    private val models = mutableListOf(
        ConfigItem(
            id = WATCH_COMPLICATION_FIRST,
            icon = emptyModelIcon,
            title = context.getString(R.string.config_complication_first_line)
        ),
        ConfigItem(
            id = WATCH_COMPLICATION_SECOND,
            icon = emptyModelIcon,
            title = context.getString(R.string.config_complication_second_line)
        ),
        ConfigItem(
            id = WATCH_COMPLICATION_THIRD,
            icon = emptyModelIcon,
            title = context.getString(R.string.config_complication_third_line)
        ),
        ConfigItem(
            id = WATCH_COMPLICATION_FOURTH,
            icon = emptyModelIcon,
            title = context.getString(R.string.config_complication_fourth_line)
        ),
        ConfigItem(
            id = WATCH_COMPLICATION_FIFTH,
            icon = emptyModelIcon,
            title = context.getString(R.string.config_complication_fifth_line)
        ),
        ConfigItem(
            id = WATCH_COMPLICATION_SIXTH,
            icon = emptyModelIcon,
            title = context.getString(R.string.config_complication_sixth_line)
        )
    )

    private var providerInfoBucket: ComplicationProviderInfoBucket? = null

    /**
     * `true` if it's a first time we loading the complication
     * info, `false` otherwise.
     */
    private var firstLoad = true

    private var lastFailed = false

    override fun onActive() {
        super.onActive()
        providerInfoRetriever = ProviderInfoRetriever(context, Executors.newCachedThreadPool())
        providerInfoRetriever.init()

        updateComplications()
    }

    fun updateComplications() {
        if (!isActive) return
        if (firstLoad || lastFailed) {
            // Show the loading progress
            // bar.
            postLoadingScreen()
        }

        providerInfoBucket?.cancel()
        providerInfoBucket =
            ComplicationProviderInfoBucket(object : ComplicationProviderInfoBucket.Callback {
                override fun onProviderInfoReceived(list: List<Data>) {
                    firstLoad = false
                    lastFailed = false

                    list.forEach { (watchFaceComplicationId, info) ->
                        val index = models.indexOfFirst { it.id == watchFaceComplicationId }
                        models[index].apply {
                            icon = info?.providerIcon?.loadDrawable(context) ?: emptyModelIcon
                            summary = info?.providerName
                        }
                    }

                    postOkScreen()
                }

                override fun onRetrievalFailed() {
                    firstLoad = false
                    lastFailed = true

                    models.forEach {
                        it.icon = emptyModelIcon
                        it.summary = null
                    }

                    postFailureScreen()
                }
            }, *WATCH_COMPLICATIONS)

        val watchFaceComponentName = ComponentName(context, WatchFaceService::class.java)
        providerInfoRetriever.retrieveProviderInfo(
            providerInfoBucket,
            watchFaceComponentName,
            *WATCH_COMPLICATIONS
        )
    }

    override fun onInactive() {
        providerInfoBucket?.cancel()

        try {
            providerInfoRetriever.release()
        } catch (e: Exception) {
            // Can happen if we failed to bind to
            // the internal service
            e.printStackTrace()
        }

        super.onInactive()
    }

    private fun postLoadingScreen() = postValue(LoadingScreen())
    private fun postFailureScreen() = postValue(FailureScreen())
    private fun postOkScreen() = postValue(OkScreen(models.toList()))

    /**
     * @author Artem Chepurnoy
     */
    private class ComplicationProviderInfoBucket(
        private var callback: Callback?,
        vararg ids: Int
    ) : ProviderInfoRetriever.OnProviderInfoReceivedCallback() {

        private val data = ArrayList<Data>()
        private val check = SparseBooleanArray().apply {
            ids.forEach {
                put(it, false)
            }
        }

        /**
         * @author Artem Chepurnoy
         */
        interface Callback {

            fun onProviderInfoReceived(list: List<Data>)

            fun onRetrievalFailed()

        }

        override fun onProviderInfoReceived(
            watchFaceComplicationId: Int,
            info: ComplicationProviderInfo?
        ) {
            check.put(watchFaceComplicationId, true)
            data += watchFaceComplicationId to info

            // Check if we retrieved all of the complication provider
            // info
            val size = check.size()
            for (i in 0 until size) {
                if (!check.valueAt(i)) return
            }

            callback?.onProviderInfoReceived(data)
        }

        override fun onRetrievalFailed() {
            callback?.onRetrievalFailed()
        }

        /**
         * After calling this method the callback is guaranteed to
         * not be called.
         */
        fun cancel() {
            callback = null
        }

    }
}
