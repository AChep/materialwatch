package com.artemchep.essence.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.artemchep.essence.R
import com.artemchep.essence.databinding.ActivityConfigBinding
import com.artemchep.essence.ui.adapter.PickerAdapter
import com.artemchep.essence.ui.interfaces.OnItemClickListener
import com.artemchep.essence.ui.model.ConfigPickerItem

/**
 * @author Artem Chepurnoy
 */
class PickerActivity : ActivityBase(), OnItemClickListener<ConfigPickerItem> {

    companion object {
        private const val EXTRA_KEY = "extra::key"
        private const val EXTRA_TITLE = "extra::title"
        private const val EXTRA_ITEMS = "extra::items"
        const val RESULT_KEY = "result::key"

        fun newIntent(
            context: Context,
            key: String,
            title: String?,
            items: ArrayList<ConfigPickerItem>
        ): Intent {
            return Intent(context, PickerActivity::class.java).apply {
                putParcelableArrayListExtra(EXTRA_ITEMS, items)
                putExtra(EXTRA_KEY, key)
                putExtra(EXTRA_TITLE, title)
            }
        }
    }

    private lateinit var adapter: PickerAdapter

    private val binding by lazy {
        ActivityConfigBinding
            .bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        val title = intent!!.getStringExtra(EXTRA_TITLE)
        val items = intent!!.getParcelableArrayListExtra<ConfigPickerItem>(EXTRA_ITEMS)
        adapter = PickerAdapter(items, title).apply {
            onItemClickListener = this@PickerActivity
        }

        binding.recyclerView.apply {
            isEdgeItemsCenteringEnabled = true
            layoutManager = LinearLayoutManager(this@PickerActivity)

            adapter = this@PickerActivity.adapter
        }
    }

    override fun onItemClick(view: View, model: ConfigPickerItem) {
        val intent = Intent().apply {
            putExtra(RESULT_KEY, model.key)
        }

        setResult(RESULT_OK, intent)
        finishAfterTransition()
    }

}