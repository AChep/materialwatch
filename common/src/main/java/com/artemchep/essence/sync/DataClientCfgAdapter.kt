package com.artemchep.essence.sync

import android.content.Context
import com.artemchep.essence.Cfg
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem

/**
 * @author Artem Chepurnoy
 */
class DataClientCfgAdapter(private val context: Context) : DataClient.OnDataChangedListener {
    override fun onDataChanged(buffer: DataEventBuffer) {
        buffer.forEach { event ->
            when (event.type) {
                DataEvent.TYPE_CHANGED -> {
                    val dataItem = DataMapItem.fromDataItem(event.dataItem)
                    onDataItemChanged(dataItem)
                }
            }
        }
    }

    private fun onDataItemChanged(dataItem: DataMapItem) {
        Cfg.edit(context) {
            // Get the changes entries and set them
            // locally.
            val dataMap = dataItem.dataMap
            dataMap.keySet().forEach { key ->
                when (key) {
                    Cfg.KEY_ACCENT_COLOR -> Cfg.accentColor = dataMap.get(key)
                    Cfg.KEY_THEME -> Cfg.themeName = dataMap.get(key)
                }
            }
        }
    }
}