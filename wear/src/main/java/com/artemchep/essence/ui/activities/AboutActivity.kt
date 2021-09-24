package com.artemchep.essence.ui.activities

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.artemchep.bindin.bindIn
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

        viewModel = ViewModelProvider(this).get(AboutViewModel::class.java)
        viewModel.setup()
    }

    private fun AboutViewModel.setup() {
        bindIn(titleTextLive, pipe = binding.titleTextView::setText)
        bindIn(contentTextLive, pipe = binding.contentTextView::setText)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.titleTextView -> viewModel.navigateTo(AboutViewModel.NavigationEvent.BUILD_INFO)
        }
    }

}