package com.artemchep.essence.live

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import com.artemchep.config.Config
import com.artemchep.essence.*
import com.artemchep.essence.domain.live.base.BaseLiveData
import com.artemchep.essence.domain.models.*
import com.artemchep.essence.ui.model.ConfigItem
import com.artemchep.mw.R

/**
 * @author Artem Chepurnoy
 */
class ScreenComplicationsEditorLiveData(
    private val context: Context,
    private val config: Cfg,
    private val watchComplicationId: Int,
    /**
     * Mapping of accent colors to
     * their names.
     */
    private val accentColorNames: Map<Int, String>,
) : BaseLiveData<List<ConfigItem>>(),
    Config.OnConfigChangedListener<String> {
    override fun onActive() {
        super.onActive()
        config.observe(this)
        update()
    }

    override fun onInactive() {
        config.removeObserver(this)
        super.onInactive()
    }

    override fun onConfigChanged(keys: Set<String>) {
        if (Cfg.KEY_COMPLICATION_EDITOR !in keys)
            return
        update()
    }

    private fun update() {
        value = generateFrom(watchComplicationId, config.complicationEditor)
    }

    private fun generateFrom(
        id: Int,
        state: ComplicationEditor,
    ): List<ConfigItem> = generateFrom(
        state = state.map[id]
            ?: ComplicationEditor.Item(),
    )

    private fun generateFrom(
        state: ComplicationEditor.Item,
    ): List<ConfigItem> {
        val iconColorName = state.iconColor
            ?.let { color -> accentColorNames[color] }
            ?: "None"
        val iconColorItem = ConfigItem(
            id = ComplicationEditor.Item.ICON_COLOR,
            icon = AppCompatResources.getDrawable(context, R.drawable.ic_palette),
            title = context.getString(R.string.config_complications_editor_icon_color),
            summary = iconColorName,
        )

        val iconEnabledItem = ConfigItem(
            id = ComplicationEditor.Item.ICON_ENABLED,
            icon = null,
            title = context.getString(R.string.config_complications_editor_icon_enabled),
            checked = state.iconEnabled ?: ComplicationEditor.Item.defaultIconEnabled,
        )

        return listOf(
            iconEnabledItem,
            iconColorItem,
        )
    }
}
