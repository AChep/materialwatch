package com.artemchep.essence.domain.live

import android.content.Context
import com.artemchep.config.Config
import com.artemchep.essence.Cfg
import com.artemchep.essence.domain.live.base.BaseLiveData
import com.artemchep.essence.domain.models.*
import com.artemchep.essence.ui.model.ConfigItem
import com.artemchep.essence_common.R

/**
 * @author Artem Chepurnoy
 */
class SettingsLiveData(
    private val context: Context,
    private val config: Cfg,
    /**
     * Collection of item ids that are emitted to
     * views.
     */
    visibleItemIds: Iterable<Int>,
    /**
     * Mapping of accent colors to
     * their names.
     */
    private val accentColorNames: Map<Int, String>,
    /**
     * Mapping of themes to
     * their names.
     */
    private val themeNames: Map<String, String>
) : BaseLiveData<Screen<List<ConfigItem>>>(),
    Config.OnConfigChangedListener<String> {

    private val keysToConfigItem = mapOf(
        SETTINGS_ITEM_COMPLICATIONS to {
            ConfigItem(
                id = SETTINGS_ITEM_COMPLICATIONS,
                icon = context.getDrawable(R.drawable.ic_view),
                title = context.getString(R.string.config_complications)
            )
        },
        SETTINGS_ITEM_ABOUT to {
            ConfigItem(
                id = SETTINGS_ITEM_ABOUT,
                icon = context.getDrawable(R.drawable.ic_information_outline),
                title = context.getString(R.string.config_about)
            )
        },
        SETTINGS_ITEM_ACCENT to {
            val accentColor = Cfg.accentColor
            val accentColorName = accentColorNames[accentColor] ?: "Unknown accent color"

            ConfigItem(
                id = SETTINGS_ITEM_ACCENT,
                icon = context.getDrawable(R.drawable.ic_palette),
                title = context.getString(R.string.config_accent),
                summary = accentColorName
            )
        },
        SETTINGS_ITEM_THEME to {
            val theme = Cfg.themeName
            val themeName = themeNames[theme] ?: "Unknown theme"

            ConfigItem(
                id = SETTINGS_ITEM_THEME,
                icon = context.getDrawable(R.drawable.ic_invert_colors),
                title = context.getString(R.string.config_theme),
                summary = themeName
            )
        }
    )

    private val configItems = visibleItemIds
        .mapNotNull { keysToConfigItem[it]?.invoke() }
        .toMutableList()

    override fun onActive() {
        super.onActive()
        config.observe(this)
        updateConfigItems()
    }

    override fun onInactive() {
        config.removeObserver(this)
        super.onInactive()
    }

    override fun onConfigChanged(keys: Set<String>) {
        val settingsKeys = keys.map(::configKeyToSettingsKey)
        updateConfigItems(settingsKeys)
    }

    private fun updateConfigItems(keys: Collection<Int> = emptyList()) {
        configItems.forEachIndexed { index, item ->
            val id = item.id
            if (id in keys || keys.isEmpty()) {
                // Update the config item
                configItems[index] = keysToConfigItem.getValue(id).invoke()
            }
        }

        postValue(OkScreen(configItems))
    }

}
