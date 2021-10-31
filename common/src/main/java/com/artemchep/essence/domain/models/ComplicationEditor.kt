package com.artemchep.essence.domain.models

import arrow.optics.optics
import com.artemchep.config.Config
import com.artemchep.config.store.StoreRead
import com.artemchep.config.store.StoreWrite
import com.artemchep.essence.Cfg
import com.squareup.moshi.JsonClass
import com.squareup.moshi.adapter

@JsonClass(generateAdapter = true)
@OptIn(ExperimentalStdlibApi::class)
@optics
data class ComplicationEditor(
    val map: Map<Int, Item> = emptyMap(),
) : Config.Record<String> {
    companion object {
        private const val KEY_JSON = "json"
    }

    @Transient
    private val adapter = Cfg.moshi.adapter<ComplicationEditor>()

    fun getOrCreate(watchComplicationId: Int) = map[watchComplicationId] ?: Item.empty

    override fun getFromStore(storeRead: StoreRead<String>): Config.Record<String> {
        return try {
            val json = storeRead.getString(KEY_JSON, "")
            adapter.fromJson(json)!!
        } catch (e: Throwable) {
            ComplicationEditor()
        }
    }

    override fun putToStore(storeWrite: StoreWrite<String>) {
        val json = adapter.toJson(this)
        storeWrite.putString(KEY_JSON, json)
    }

    @JsonClass(generateAdapter = true)
    @optics
    data class Item(
        val rotation: Float? = null,
        val iconColor: Int? = null,
        val iconEnabled: Boolean? = null,
    ) {
        companion object {
            val empty = Item()

            val defaultIconEnabled = true

            const val ICON_COLOR = 1
            const val ICON_ENABLED = 2
        }
    }
}
