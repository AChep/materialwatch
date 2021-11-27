package com.artemchep.essence.viewmodel

import android.app.Application
import android.graphics.Color
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import arrow.core.plus
import com.artemchep.essence.Cfg
import com.artemchep.essence.domain.models.*
import com.artemchep.essence.domain.viewmodel.SettingsViewModel
import com.artemchep.essence.domain.viewmodel.base.BaseViewModel
import com.artemchep.essence.domain.viewmodel.paletteMap
import com.artemchep.essence.live.ScreenComplicationsEditorLiveData
import com.artemchep.essence.ui.PALETTE_TRANSPARENT
import com.artemchep.essence.ui.model.ConfigItem
import com.artemchep.essence.ui.model.ConfigPickerItem
import com.artemchep.mw.R

/**
 * @author Artem Chepurnoy
 */
class ComplicationEditorViewModel(
    application: Application,
    private val watchComplicationId: Int,
) : BaseViewModel(application) {
    companion object {
        private const val REQUEST_CODE_ICON_COLOR = 100
    }

    class Factory(
        private val application: Application,
        private val watchComplicationId: Int,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ComplicationEditorViewModel(
                application = application,
                watchComplicationId = watchComplicationId,
            ) as T
    }

    /**
     * Map of the palette and its
     * names.
     */
    private val paletteMap = paletteMap(context)
        .let {
            val noneColor = PALETTE_TRANSPARENT to context.getString(R.string.none)
            mapOf(noneColor) + it
        }

    val screenLiveData = ScreenComplicationsEditorLiveData(
        context = context,
        config = Cfg,
        watchComplicationId = watchComplicationId,
        accentColorNames = paletteMap,
    )

    val showPickerEvent = MutableLiveData<Event<PickerSource>>()

    fun result(requestCode: Int, key: String?) {
        if (key == null) {
            return
        }

        Cfg.edit(context) {
            when (requestCode) {
                REQUEST_CODE_ICON_COLOR -> {
                    val iconColor = key.toInt()
                        .takeUnless { it == PALETTE_TRANSPARENT }
                    Cfg.modifyComplicationEditorItem { item ->
                        item.copy(iconColor = iconColor)
                    }
                }
            }
        }
    }

    private fun Cfg.modifyComplicationEditorItem(
        block: (ComplicationEditor.Item) -> ComplicationEditor.Item,
    ) {
        complicationEditor = ComplicationEditor.map.modify(complicationEditor) { map ->
            map
                .toMutableMap()
                .apply {
                    val new = map[watchComplicationId]
                        ?: ComplicationEditor.Item()
                    put(watchComplicationId, block(new))
                }
        }
    }

    fun showDetails(item: ConfigItem) {
        when (item.id) {
            ComplicationEditor.Item.ICON_COLOR -> {
                val selectedKey = Cfg.complicationEditor.getOrCreate(watchComplicationId).iconColor
                    ?: PALETTE_TRANSPARENT
                val data = PickerSource(
                    title = context.getString(R.string.config_complications_editor_icon_color),
                    items = paletteMap.map { (color, name) ->
                        ConfigPickerItem(
                            color.toString(),
                            color,
                            name
                        )
                    },
                    selectedKey = selectedKey.toString(),
                    requestCode = REQUEST_CODE_ICON_COLOR,
                )
                val event = Event(data)
                showPickerEvent.postValue(event)
            }
            ComplicationEditor.Item.ICON_ENABLED -> {
                Cfg.edit(context) {
                    Cfg.modifyComplicationEditorItem { item ->
                        val wasIconEnabled = item.iconEnabled
                            ?: ComplicationEditor.Item.defaultIconEnabled
                        item.copy(iconEnabled = !wasIconEnabled)
                    }
                }
            }
        }
    }

}
