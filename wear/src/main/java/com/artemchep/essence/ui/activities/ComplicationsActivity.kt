package com.artemchep.essence.ui.activities

import android.content.ComponentName
import android.os.Bundle
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.ComplicationHelperActivity
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.artemchep.bindin.bindIn
import com.artemchep.mw.R
import com.artemchep.mw.databinding.ActivityConfigComplicationsBinding
import com.artemchep.essence.domain.models.FailureScreen
import com.artemchep.essence.domain.models.LoadingScreen
import com.artemchep.essence.domain.models.OkScreen
import com.artemchep.essence.service.WatchFaceService
import com.artemchep.essence.ui.adapter.MainAdapter
import com.artemchep.essence.ui.interfaces.OnItemClickListener
import com.artemchep.essence.ui.model.ConfigItem
import com.artemchep.essence.viewmodel.ComplicationViewModel

/**
 * @author Artem Chepurnoy
 */
class ComplicationsActivity : ActivityBase(), OnItemClickListener<ConfigItem> {

    private lateinit var adapter: MainAdapter

    private lateinit var viewModel: ComplicationViewModel

    private val binding by lazy {
        ActivityConfigComplicationsBinding
            .bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config_complications)

        val title = getString(R.string.config_complications)
        adapter = MainAdapter(mutableListOf(), title).apply {
            onItemClickListener = this@ComplicationsActivity
        }

        binding.complicationsRecyclerView.apply {
            isEdgeItemsCenteringEnabled = true
            layoutManager = LinearLayoutManager(this@ComplicationsActivity)

            adapter = this@ComplicationsActivity.adapter
        }

        viewModel = ViewModelProvider(this).get(ComplicationViewModel::class.java)
        viewModel.setup()
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateComplications()
    }

    private fun ComplicationViewModel.setup() {
        bindIn(screenLiveData) { screen ->
            binding.complicationsRecyclerView.isVisible = screen is OkScreen
            binding.progressView.isVisible = screen is LoadingScreen
            binding.errorView.isVisible = screen is FailureScreen

            when (screen) {
                is OkScreen -> {
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
        bindIn(showProviderForComplicationEvent) {
            val watchFaceComplicationId = it.consume()
            if (watchFaceComplicationId != null) {
                val supportedTypes = intArrayOf(
                    ComplicationData.TYPE_RANGED_VALUE,
                    ComplicationData.TYPE_ICON,
                    ComplicationData.TYPE_SHORT_TEXT,
                    ComplicationData.TYPE_SMALL_IMAGE
                )

                val watchFace =
                    ComponentName(this@ComplicationsActivity, WatchFaceService::class.java)
                val intent = ComplicationHelperActivity.createProviderChooserHelperIntent(
                    this@ComplicationsActivity,
                    watchFace,
                    watchFaceComplicationId,
                    *supportedTypes
                )
                startActivity(intent)
            }
        }
    }

    override fun onItemClick(view: View, model: ConfigItem) {
        viewModel.showProviderForComplication(model)
    }

}