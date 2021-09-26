package com.artemchep.essence.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.artemchep.mw.R
import com.artemchep.essence.domain.models.PickerSource
import com.artemchep.essence.ui.model.ConfigPickerItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * @author Artem Chepurnoy
 */
class PickerDialog : DialogFragment() {

    companion object {
        const val TAG = "PickerDialog"

        private const val EXTRA_KEY = "extra::key"
        private const val EXTRA_TITLE = "extra::title"
        private const val EXTRA_ITEMS = "extra::items"
        private const val EXTRA_RC = "extra::rc"

        fun create(data: PickerSource): PickerDialog {
            return PickerDialog().apply {
                val bundle = Bundle().apply {
                    putParcelableArrayList(EXTRA_ITEMS, ArrayList(data.items))
                    putString(EXTRA_KEY, data.selectedKey)
                    putString(EXTRA_TITLE, data.title)
                    putInt(EXTRA_RC, data.requestCode)
                }

                arguments = bundle
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = requireArguments()

        val requestCode = args.getInt(EXTRA_RC)
        val title = args.getString(EXTRA_TITLE)!!
        val items = args.getParcelableArrayList<ConfigPickerItem>(EXTRA_ITEMS)!!

        val mdItems = items.map { it.title }.toTypedArray()
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setItems(mdItems) { dialog, which ->
                val a = activity
                if (a is PickerDialogCallback) {
                    a.onSingleItemPicked(requestCode, items[which].key)
                    dialog.dismiss()
                } else {
                    throw IllegalStateException("Activity does not implement PickerDialogCallback")
                }
            }
            .setNegativeButton(R.string.action_close) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    /**
     * @author Artem Chepurnoy
     */
    interface PickerDialogCallback {

        fun onSingleItemPicked(requestCode: Int, key: String?)

    }

}