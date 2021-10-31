package com.artemchep.essence.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.artemchep.bindin.bindIn
import com.artemchep.essence.Cfg
import com.artemchep.essence.domain.models.*
import com.artemchep.essence.domain.viewmodel.SettingsViewModel
import com.artemchep.mw.R
import com.artemchep.mw.databinding.ActivityConfigComplicationEditorBinding
import com.artemchep.essence.ui.adapter.MainAdapter
import com.artemchep.essence.ui.interfaces.OnItemClickListener
import com.artemchep.essence.ui.model.ConfigItem
import com.artemchep.essence.ui.model.ConfigPickerItem
import com.artemchep.essence.viewmodel.ComplicationEditorViewModel
import com.artemchep.essence.viewmodel.ComplicationViewModel

/**
 * @author Artem Chepurnoy
 */
class ComplicationEditorActivity : ActivityBase(), OnItemClickListener<ConfigItem> {

    companion object {
        private const val EXTRA_COMPLICATION_ID = "extra::id"

        fun newIntent(
            context: Context,
            id: Int,
        ): Intent {
            return Intent(context, ComplicationEditorActivity::class.java).apply {
                putExtra(EXTRA_COMPLICATION_ID, id)
            }
        }
    }

    private lateinit var adapter: MainAdapter

    private lateinit var viewModel: ComplicationEditorViewModel

    private val binding by lazy {
        ActivityConfigComplicationEditorBinding
            .bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config_complication_editor)

        val title = getString(R.string.config_complications_editor)
        adapter = MainAdapter(mutableListOf(), title).apply {
            onItemClickListener = this@ComplicationEditorActivity
        }

        binding.recyclerView.apply {
            isEdgeItemsCenteringEnabled = true
            layoutManager = LinearLayoutManager(this@ComplicationEditorActivity)

            adapter = this@ComplicationEditorActivity.adapter
        }

        val watchComplicationId = intent!!.getIntExtra(EXTRA_COMPLICATION_ID, 0)
        viewModel = ViewModelProvider(
            this,
            ComplicationEditorViewModel.Factory(
                application = application,
                watchComplicationId = watchComplicationId,
            )
        ).get(ComplicationEditorViewModel::class.java)
        viewModel.setup()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun ComplicationEditorViewModel.setup() {
        bindIn(screenLiveData) { list ->
            adapter.apply {
                models.apply {
                    clear()
                    addAll(list)
                }
                notifyDataSetChanged()
            }
        }
        bindIn(showPickerEvent) { event ->
            val data = event.consume()
            if (data != null) {
                val intent = PickerActivity.newIntent(
                    this@ComplicationEditorActivity,
                    data.selectedKey,
                    data.title,
                    ArrayList(data.items)
                )
                startActivityForResult(intent, data.requestCode)
            }
        }
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