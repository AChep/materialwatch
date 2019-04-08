package com.artemchep.essence.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.artemchep.essence.R
import com.artemchep.essence.domain.viewmodel.AboutViewModel

/**
 * @author Artem Chepurnoy
 */
class AboutDialog : DialogFragment() {

    companion object {
        const val TAG = "AboutDialog"

        private const val EMPTY_TEXT = ""
    }

    private lateinit var materialDialog: MaterialDialog

    private lateinit var viewModel: AboutViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        materialDialog = MaterialDialog(context!!)
            .title(text = EMPTY_TEXT) // to inject content view into layout
            .message(text = EMPTY_TEXT) // to inject content view into layout
            .negativeButton(res = R.string.action_close)

        // Bind the view-model
        viewModel = ViewModelProviders.of(this).get(AboutViewModel::class.java)
        viewModel.setup()
        return materialDialog
    }

    private fun AboutViewModel.setup() {
        titleTextLive.observe(this@AboutDialog, Observer {
            materialDialog.title(text = it.toString())
        })
        contentTextLive.observe(this@AboutDialog, Observer {
            materialDialog.message(text = it.toString())
        })
    }

}