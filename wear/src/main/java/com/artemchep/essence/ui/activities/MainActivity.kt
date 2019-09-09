package com.artemchep.essence.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.artemchep.essence.ACTION_PERMISSIONS_CHANGED
import com.artemchep.essence.Cfg
import com.artemchep.essence.R
import com.artemchep.essence.domain.models.*
import com.artemchep.essence.domain.viewmodel.SettingsViewModel
import com.artemchep.essence.ui.adapter.MainAdapter
import com.artemchep.essence.ui.interfaces.OnItemClickListener
import com.artemchep.essence.ui.model.ConfigItem
import kotlinx.android.synthetic.main.activity_config.*

/**
 * @author Artem Chepurnoy
 */
class MainActivity : ActivityBase(), OnItemClickListener<ConfigItem> {

    companion object {
        private const val RUNTIME_PERMISSIONS_REQUEST_CODE = 100
    }

    private lateinit var adapter: MainAdapter

    private lateinit var viewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        val title = getString(R.string.config)
        adapter = MainAdapter(mutableListOf(), title).apply {
            onItemClickListener = this@MainActivity
        }

        recyclerView.apply {
            isEdgeItemsCenteringEnabled = true
            layoutManager = LinearLayoutManager(this@MainActivity)

            adapter = this@MainActivity.adapter
        }

        val itemIds = setOf(
            SETTINGS_ITEM_COMPLICATIONS,
            SETTINGS_ITEM_THEME,
            SETTINGS_ITEM_ACCENT,
            SETTINGS_ITEM_ABOUT
        )
        viewModel = ViewModelProviders
            .of(this, SettingsViewModel.Factory(application, Cfg, itemIds))
            .get(SettingsViewModel::class.java)
        viewModel.setup()

        setupRuntimePermissions()
    }

    private fun SettingsViewModel.setup() {
        screenLiveData.observe(this@MainActivity, Observer { screen ->
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
        })
        showDetailsEvent.observe(this@MainActivity, Observer { event ->
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
        })
        showPickerEvent.observe(this@MainActivity, Observer { event ->
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
        })
        showGrantRuntimePermissionsEvent.observe(this@MainActivity, Observer { event ->
            val data = event.consume()
            if (data != null) {
                val activity = this@MainActivity
                ActivityCompat.requestPermissions(
                    activity,
                    data.permissions.toTypedArray(),
                    data.requestCode
                )
            }
        })
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