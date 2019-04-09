package com.artemchep.essence.ui.activities

import android.content.ComponentName
import android.os.Bundle
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.ComplicationHelperActivity
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.artemchep.essence.R
import com.artemchep.essence.domain.models.FailureScreen
import com.artemchep.essence.domain.models.LoadingScreen
import com.artemchep.essence.domain.models.OkScreen
import com.artemchep.essence.service.WatchFaceService
import com.artemchep.essence.ui.adapter.MainAdapter
import com.artemchep.essence.ui.interfaces.OnItemClickListener
import com.artemchep.essence.ui.model.ConfigItem
import com.artemchep.essence.viewmodel.ComplicationViewModel
import kotlinx.android.synthetic.main.activity_config_complications.*

/**
 * @author Artem Chepurnoy
 */
class ComplicationsActivity : ActivityBase(), OnItemClickListener<ConfigItem> {

    private lateinit var adapter: MainAdapter

    private lateinit var viewModel: ComplicationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config_complications)

        val title = getString(R.string.config_complications)
        adapter = MainAdapter(mutableListOf(), title).apply {
            onItemClickListener = this@ComplicationsActivity
        }

        complicationsRecyclerView.apply {
            isEdgeItemsCenteringEnabled = true
            layoutManager = LinearLayoutManager(this@ComplicationsActivity)

            adapter = this@ComplicationsActivity.adapter
        }

        viewModel = ViewModelProviders.of(this).get(ComplicationViewModel::class.java)
        viewModel.setup()
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateComplications()
    }

    private fun ComplicationViewModel.setup() {
        screenLiveData.observe(this@ComplicationsActivity, Observer { screen ->
            complicationsRecyclerView.isVisible = screen is OkScreen
            progressView.isVisible = screen is LoadingScreen
            errorView.isVisible = screen is FailureScreen

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
        })
        showProviderForComplicationEvent.observe(this@ComplicationsActivity, Observer {
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
        })
    }

    override fun onItemClick(view: View, model: ConfigItem) {
        viewModel.showProviderForComplication(model)
    }

}