package com.artemchep.essence.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.artemchep.bindin.bindIn
import com.artemchep.essence.R
import com.artemchep.essence.domain.viewmodel.AboutViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * @author Artem Chepurnoy
 */
class AboutDialog : DialogFragment() {

    companion object {
        const val TAG = "AboutDialog"

        private const val EMPTY_TEXT = ""
    }

    private lateinit var materialDialog: AlertDialog

    private lateinit var viewModel: AboutViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        materialDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(EMPTY_TEXT) // to inject content view into layout
            .setMessage(EMPTY_TEXT) // to inject content view into layout
            .setNegativeButton(R.string.action_close) { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        // Bind the view-model
        viewModel = ViewModelProvider(this).get(AboutViewModel::class.java)
        viewModel.setup()
        return materialDialog
    }

    private fun AboutViewModel.setup() {
        bindIn(titleTextLive, pipe = materialDialog::setTitle)
        bindIn(contentTextLive, pipe = materialDialog::setMessage)
    }

}