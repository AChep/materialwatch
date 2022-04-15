package com.artemchep.essence.domain.live

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import com.artemchep.config.Config
import com.artemchep.essence.Cfg
import com.artemchep.essence.domain.live.base.BaseLiveData
import com.artemchep.essence.domain.models.*
import com.artemchep.essence.ui.model.ConfigItem
import com.artemchep.mw_common.R

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
                icon = AppCompatResources.getDrawable(context, R.drawable.ic_view),
                summary = context.getString(R.string.config_complications_disclaimer),
                title = context.getString(R.string.config_complications)
            )
        },
        SETTINGS_ITEM_ABOUT to {
            ConfigItem(
                id = SETTINGS_ITEM_ABOUT,
                icon = AppCompatResources.getDrawable(context, R.drawable.ic_information_outline),
                title = context.getString(R.string.config_about)
            )
        },
        SETTINGS_ITEM_DIGITAL_CLOCK to {
            val isChecked = Cfg.digitalClockEnabled
            ConfigItem(
                id = SETTINGS_ITEM_DIGITAL_CLOCK,
                icon = AppCompatResources.getDrawable(context, R.drawable.ic_clock),
                title = context.getString(R.string.config_digital_clock),
                checked = isChecked,
            )
        },
        SETTINGS_ITEM_HANDS_REVERTED to {
            val isChecked = Cfg.handsReverted
            ConfigItem(
                id = SETTINGS_ITEM_HANDS_REVERTED,
                icon = null,
                title = context.getString(R.string.config_hands_reverted),
                checked = isChecked,
            )
        },
        SETTINGS_ITEM_COMPLICATION_ALWAYS_ON to {
            val isChecked = Cfg.complicationAlwaysOn
            ConfigItem(
                id = SETTINGS_ITEM_COMPLICATION_ALWAYS_ON,
                icon = null,
                title = context.getString(R.string.config_always_show_complications),
                checked = isChecked,
            )
        },
        SETTINGS_ITEM_ACCENT_TINT_BG to {
            val isChecked = Cfg.accentBgEnabled
            ConfigItem(
                id = SETTINGS_ITEM_ACCENT_TINT_BG,
                title = context.getString(R.string.config_accent_bg),
                checked = isChecked,
            )
        },
        SETTINGS_ITEM_ACCENT to {
            val accentColor = Cfg.accentColor
            val accentColorName = accentColorNames[accentColor] ?: "Unknown accent color"

            ConfigItem(
                id = SETTINGS_ITEM_ACCENT,
                icon = AppCompatResources.getDrawable(context, R.drawable.ic_palette),
                title = context.getString(R.string.config_accent),
                summary = accentColorName
            )
        },
        SETTINGS_ITEM_THEME to {
            val theme = Cfg.themeName
            val themeName = themeNames[theme] ?: "Unknown theme"

            ConfigItem(
                id = SETTINGS_ITEM_THEME,
                icon = AppCompatResources.getDrawable(context, R.drawable.ic_theme),
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
