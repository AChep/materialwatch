package com.artemchep.essence.ui.activities

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.artemchep.bindin.bindIn
import com.artemchep.config.Config
import com.artemchep.essence.ACTION_PERMISSIONS_CHANGED
import com.artemchep.essence.Cfg
import com.artemchep.essence.domain.models.*
import com.artemchep.mw.R
import com.artemchep.mw.databinding.ActivityMainBinding
import com.artemchep.essence.domain.viewmodel.SettingsViewModel
import com.artemchep.essence.flow.PreviewAmbientModeFlow
import com.artemchep.essence.flow.PreviewTimeFlow
import com.artemchep.essence.sync.DataClientCfgAdapter
import com.artemchep.essence.ui.adapters.MainAdapter
import com.artemchep.essence.ui.dialogs.AboutDialog
import com.artemchep.essence.ui.dialogs.PickerDialog
import com.artemchep.essence.ui.drawables.*
import com.artemchep.essence.ui.interfaces.OnItemClickListener
import com.artemchep.essence.ui.model.ConfigItem
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * @author Artem Chepurnoy
 */
class MainActivity : ActivityBase(),
    PickerDialog.PickerDialogCallback,
    OnItemClickListener<ConfigItem>,
    View.OnClickListener,
    Config.OnConfigChangedListener<String> {

    // ---- Setup ----

    private lateinit var dataClient: DataClient

    private lateinit var adapter: MainAdapter

    private lateinit var settingsViewModel: SettingsViewModel

    private val dataClientCfgAdapter by lazy { DataClientCfgAdapter(this) }

    private val analogClockDrawable by lazy { AnalogClockDrawable(this) }

    private val binding by lazy {
        ActivityMainBinding
            .bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dataClient = Wearable.getDataClient(this)

        setupView()
        setupRuntimePermissions()
    }

    private fun setupView() {
        binding.toolbarView?.actionAboutBtn?.setOnClickListener(this)

        adapter = MainAdapter(mutableListOf()).apply {
            // Handle item click
            onItemClickListener = this@MainActivity
        }

        binding.watchFace.background = ClipCircleDrawable(analogClockDrawable)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }

        setupSettingsViewModel()
    }

    private fun invalidate() {
        analogClockDrawable.invalidateSelf()
    }

    private fun setupSettingsViewModel() {
        val settingsViewModelFactory = SettingsViewModel.Factory(
            application, Cfg,
            setOf(
                SETTINGS_ITEM_DIGITAL_CLOCK,
                SETTINGS_ITEM_HANDS_REVERTED,
                SETTINGS_ITEM_COMPLICATION_ALWAYS_ON,
                SETTINGS_ITEM_THEME,
                SETTINGS_ITEM_ACCENT,
                SETTINGS_ITEM_ACCENT_TINT_BG,
            )
        )
        settingsViewModel = ViewModelProvider(this, settingsViewModelFactory)
            .get(SettingsViewModel::class.java)
        settingsViewModel.setup()
    }

    private fun SettingsViewModel.setup() {
        bindIn(screenLiveData) { screen ->
            when (screen) {
                is OkScreen<List<ConfigItem>> -> {
                    adapter.apply {
                        models.apply {
                            clear()
                            addAll(screen.data)
                        }
                        notifyDataSetChanged()
                    }
                }
            }
        }
        bindIn(showPickerEvent) { event ->
            val data = event.consume()
            if (data != null) {
                val dialog = PickerDialog.create(data)
                dialog.show(supportFragmentManager, PickerDialog.TAG)
            }
        }
        bindIn(showGrantRuntimePermissionsEvent) { event ->
            val data = event.consume()
            if (data != null) {
                val activity = this@MainActivity
                ActivityCompat.requestPermissions(
                    activity,
                    data.permissions.toTypedArray(),
                    data.requestCode
                )
            }
        }
    }

    private fun setupRuntimePermissions() {
        settingsViewModel.ensureRuntimePermissions()
    }

    override fun onStart() {
        super.onStart()
        dataClient.addListener(dataClientCfgAdapter)

        val timeSink = PreviewTimeFlow()
        val ambientSink = PreviewAmbientModeFlow()
            .stateIn(this, SharingStarted.WhileSubscribed(), false)
        analogClockDrawable.installCfgIn(this, ::invalidate)
        analogClockDrawable.installTimeIn(
            scope = this,
            timeFlow = timeSink,
            ambientFlow = ambientSink,
            invalidate = ::invalidate,
        )
        analogClockDrawable.installAmbientIn(
            scope = this,
            ambientFlow = ambientSink,
            invalidate = ::invalidate,
        )
    }

    override fun onResume() {
        super.onResume()
        Cfg.observe(this)
        updateAppAccentColor()
    }

    override fun onPause() {
        Cfg.removeObserver(this)
        super.onPause()
    }

    override fun onStop() {
        dataClient.removeListener(dataClientCfgAdapter)
        super.onStop()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val localBroadcastManager = LocalBroadcastManager.getInstance(this@MainActivity)
        val intent = Intent(ACTION_PERMISSIONS_CHANGED)
        localBroadcastManager.sendBroadcast(intent)
    }

    override fun onConfigChanged(keys: Set<String>) {
        if (Cfg.KEY_ACCENT_COLOR in keys) {
            updateAppAccentColor()
        }
    }

    private fun updateAppAccentColor(accentColor: Int = Cfg.accentColor) {
        val isContentColorDark = ColorUtils.calculateLuminance(accentColor) < 0.5
        val contentColor = if (isContentColorDark) {
            Color.WHITE
        } else Color.BLACK

        // Change the color of toolbar
        binding.appbarView.setBackgroundColor(accentColor)
        binding.toolbarView?.titleTextView?.setTextColor(contentColor)
        binding.toolbarView?.actionAboutBtn?.imageTintList = ColorStateList.valueOf(contentColor)

        // Change the color of status bar
        window.apply {
            statusBarColor = accentColor
            val visibility = if (isContentColorDark) {
                decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            } else decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            decorView.systemUiVisibility = visibility
        }
    }

    override fun onSingleItemPicked(requestCode: Int, key: String?) {
        settingsViewModel.result(requestCode, key)
    }

    override fun onItemClick(view: View, model: ConfigItem) {
        settingsViewModel.showDetails(model)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.actionAboutBtn -> {
                val dialog = AboutDialog()
                dialog.show(supportFragmentManager, AboutDialog.TAG)
            }
        }
    }

}
