package com.artemchep.essence.domain.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.artemchep.essence.Cfg
import com.artemchep.essence.domain.live.SettingsLiveData
import com.artemchep.essence.domain.models.*
import com.artemchep.essence.domain.viewmodel.base.BaseViewModel
import com.artemchep.essence.ui.*
import com.artemchep.essence.ui.model.ConfigItem
import com.artemchep.essence.ui.model.ConfigPickerItem
import com.artemchep.essence_common.R

/**
 * @author Artem Chepurnoy
 */
class SettingsViewModel(
    application: Application,
    config: Cfg,
    keys: Set<Int>
) : BaseViewModel(application) {

    companion object {
        private const val REQUEST_CODE_ACCENT_COLOR = 100
        private const val REQUEST_CODE_THEME = 101
    }

    /**
     * Map of the palette and its
     * names.
     */
    private val paletteMap = mapOf(
        PALETTE_RED to context.getString(R.string.red),
        PALETTE_PINK to context.getString(R.string.pink),
        PALETTE_PURPLE to context.getString(R.string.purple),
        PALETTE_DEEP_PURPLE to context.getString(R.string.deep_purple),
        PALETTE_INDIGO to context.getString(R.string.indigo),
        PALETTE_BLUE to context.getString(R.string.blue),
        PALETTE_CYAN to context.getString(R.string.cyan),
        PALETTE_TEAL to context.getString(R.string.teal),
        PALETTE_GREEN to context.getString(R.string.green),
        PALETTE_LIGHT_GREEN to context.getString(R.string.light_green),
        PALETTE_LIME to context.getString(R.string.lime),
        PALETTE_YELLOW to context.getString(R.string.yellow),
        PALETTE_AMBER to context.getString(R.string.amber),
        PALETTE_ORANGE to context.getString(R.string.orange),
        PALETTE_DEEP_ORANGE to context.getString(R.string.deep_orange),
        PALETTE_BROWN to context.getString(R.string.brown),
        PALETTE_GREY to context.getString(R.string.grey),
        PALETTE_WHITE to context.getString(R.string.white)
    )

    /**
     * Map of the themes and its
     * names
     */
    private val themeMap = mapOf(
        Cfg.THEME_BLACK to context.getString(R.string.theme_black),
        Cfg.THEME_DARK to context.getString(R.string.theme_dark),
        Cfg.THEME_LIGHT to context.getString(R.string.theme_light)
    )

    val screenLiveData = SettingsLiveData(
        context,
        config,
        keys,
        accentColorNames = paletteMap,
        themeNames = themeMap
    )

    val showDetailsEvent = MutableLiveData<Event<Int>>()

    val showPickerEvent = MutableLiveData<Event<PickerSource>>()

    fun result(requestCode: Int, key: String?) {
        if (key == null) {
            return
        }

        Cfg.edit(context) {
            when (requestCode) {
                REQUEST_CODE_THEME -> Cfg.themeName = key
                REQUEST_CODE_ACCENT_COLOR -> {
                    val color = key.toInt()
                    Cfg.accentColor = color
                }
            }
        }
    }

    fun showDetails(item: ConfigItem) {
        when (item.id) {
            SETTINGS_ITEM_COMPLICATIONS,
            SETTINGS_ITEM_ABOUT -> {
                val event = Event(item.id)
                showDetailsEvent.postValue(event)
            }
            SETTINGS_ITEM_THEME -> {
                val data = PickerSource(
                    title = context.getString(R.string.config_theme),
                    items = themeMap.map { (key, name) ->
                        val color = when (key) {
                            Cfg.THEME_BLACK -> Theme.BLACK
                            Cfg.THEME_DARK -> Theme.DARK
                            else -> Theme.LIGHT
                        }.backgroundColor

                        // Convert to the picker item
                        ConfigPickerItem(key, color, name)
                    },
                    selectedKey = Cfg.themeName,
                    requestCode = REQUEST_CODE_THEME
                )
                val event = Event(data)
                showPickerEvent.postValue(event)
            }
            SETTINGS_ITEM_ACCENT -> {
                val data = PickerSource(
                    title = context.getString(R.string.config_accent),
                    items = paletteMap.map { (color, name) ->
                        ConfigPickerItem(
                            color.toString(),
                            color,
                            name
                        )
                    },
                    selectedKey = Cfg.accentColor.toString(),
                    requestCode = REQUEST_CODE_ACCENT_COLOR
                )
                val event = Event(data)
                showPickerEvent.postValue(event)
            }
        }
    }

    /**
     * @author Artem Chepurnoy
     */
    class Factory(
        private val application: Application,
        /**
         * An instance of the config, used by
         * this application.
         */
        private val config: Cfg,
        private val keys: Set<Int>
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return when {
                modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                    val viewModel = SettingsViewModel(
                        application,
                        config,
                        keys
                    )
                    viewModel as T
                }
                else -> throw IllegalArgumentException()
            }
        }
    }

}
