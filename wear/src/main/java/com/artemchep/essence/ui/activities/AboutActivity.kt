package com.artemchep.essence.ui.activities

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.artemchep.essence.R
import com.artemchep.essence.domain.viewmodel.AboutViewModel
import kotlinx.android.synthetic.main.activity_config_about.*

/**
 * @author Artem Chepurnoy
 */
class AboutActivity : ActivityBase(), View.OnClickListener {

    private lateinit var viewModel: AboutViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config_about)

        titleTextView.setOnClickListener(this)

        viewModel = ViewModelProviders.of(this).get(AboutViewModel::class.java)
        viewModel.setup()
    }

    private fun AboutViewModel.setup() {
        titleTextLive.observe(this@AboutActivity, Observer(titleTextView::setText))
        contentTextLive.observe(this@AboutActivity, Observer(contentTextView::setText))
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.titleTextView -> viewModel.navigateTo(AboutViewModel.NavigationEvent.BUILD_INFO)
        }
    }

}