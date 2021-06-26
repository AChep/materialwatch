package com.artemchep.essence.ui.activities

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.artemchep.essence.R
import com.artemchep.essence.databinding.ActivityConfigAboutBinding
import com.artemchep.essence.domain.viewmodel.AboutViewModel

/**
 * @author Artem Chepurnoy
 */
class AboutActivity : ActivityBase(), View.OnClickListener {

    private lateinit var viewModel: AboutViewModel

    private val binding by lazy {
        ActivityConfigAboutBinding
            .bind(findViewById<ViewGroup>(android.R.id.content).getChildAt(0))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config_about)

        binding.titleTextView.setOnClickListener(this)

        viewModel = ViewModelProviders.of(this).get(AboutViewModel::class.java)
        viewModel.setup()
    }

    private fun AboutViewModel.setup() {
        titleTextLive.observe(this@AboutActivity, Observer(binding.titleTextView::setText))
        contentTextLive.observe(this@AboutActivity, Observer(binding.contentTextView::setText))
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.titleTextView -> viewModel.navigateTo(AboutViewModel.NavigationEvent.BUILD_INFO)
        }
    }

}