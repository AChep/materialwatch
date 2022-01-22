package com.artemchep.essence.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.artemchep.bindin.bindIn
import com.artemchep.essence.ACTION_PERMISSIONS_CHANGED
import com.artemchep.essence.Cfg
import com.artemchep.mw.R
import com.artemchep.mw.databinding.ActivityConfigBinding
import com.artemchep.essence.domain.models.*
import com.artemchep.essence.domain.viewmodel.SettingsViewModel
import com.artemchep.essence.ui.adapter.MainAdapter
import com.artemchep.essence.ui.interfaces.OnItemClickListener
import com.artemchep.essence.ui.model.ConfigItem

/**
 * @author Artem Chepurnoy
 */
class MainActivity : ActivityBase(), OnItemClickListener<ConfigItem> {

    companion object {
        private const val RUNTIME_PERMISSIONS_REQUEST_CODE = 100
    }

    private lateinit var adapter: MainAdapter

    private lateinit var viewModel: SettingsViewModel

    private val binding by lazy {
        ActivityConfigBinding
            .bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        val title = getString(R.string.config)
        adapter = MainAdapter(mutableListOf(), title).apply {
            onItemClickListener = this@MainActivity
        }

        binding.recyclerView.apply {
            isEdgeItemsCenteringEnabled = true
            layoutManager = LinearLayoutManager(this@MainActivity)

            adapter = this@MainActivity.adapter
        }

        val itemIds = setOfNotNull(
            SETTINGS_ITEM_COMPLICATIONS
                .takeIf { resources.configuration.isScreenRound },
            SETTINGS_ITEM_DIGITAL_CLOCK,
            SETTINGS_ITEM_HANDS_REVERTED,
            SETTINGS_ITEM_THEME,
            SETTINGS_ITEM_ACCENT,
            SETTINGS_ITEM_ACCENT_TINT_BG,
            SETTINGS_ITEM_ABOUT
        )
        viewModel = kotlin.run {
            val factory = SettingsViewModel.Factory(application, Cfg, itemIds)
            ViewModelProvider(this, factory)
                .get(SettingsViewModel::class.java)
        }
        viewModel.setup()

        setupRuntimePermissions()
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
        bindIn(showDetailsEvent) { event ->
            val id = event.consume()
            when (id) {
                SETTINGS_ITEM_COMPLICATIONS -> {
                    val intent = Intent(this@MainActivity, ComplicationsActivity::class.java)
                    startActivity(intent)
                }
                SETTINGS_ITEM_ABOUT -> {
                    val intent = Intent(this@MainActivity, AboutActivity::class.java)
                    startActivity(intent)
                }
            }
        }
        bindIn(showPickerEvent) { event ->
            val data = event.consume()
            if (data != null) {
                val intent = PickerActivity.newIntent(
                    this@MainActivity,
                    data.selectedKey,
                    data.title,
                    ArrayList(data.items)
                )
                startActivityForResult(intent, data.requestCode)
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
        viewModel.ensureRuntimePermissions()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val key = data?.getStringExtra(PickerActivity.RESULT_KEY)
        viewModel.result(requestCode, key)
    }

    override fun onItemClick(view: View, model: ConfigItem) {
        viewModel.showDetails(model)
    }

}