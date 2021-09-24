package com.artemchep.essence.ui.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.ColorUtils
import androidx.core.widget.ImageViewCompat
import com.artemchep.essence.R
import com.artemchep.essence.ui.adapters.AdapterBase
import com.artemchep.essence.ui.interfaces.OnItemClickListener
import com.artemchep.essence.ui.model.ConfigPickerItem
import com.google.android.material.card.MaterialCardView

/**
 * @author Artem Chepurnoy
 */
open class PickerAdapter(
    models: MutableList<ConfigPickerItem>,
    title: CharSequence?
) :
    AdapterTitled<ConfigPickerItem, PickerAdapter.Holder>(models, title) {

    override val binderItem = object : Binder<Holder>() {

        override fun createView(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): View {
            return inflater.inflate(R.layout.item_color, parent, false)
        }

        override fun createViewHolder(itemView: View, viewType: Int): Holder {
            return Holder(itemView, this@PickerAdapter)
        }

        override fun bindViewHolder(holder: Holder, position: Int) {
            val model = getItem(position)
            val colorIsDark = ColorUtils.calculateLuminance(model.color) < 0.5f
            val colorContent = if (colorIsDark) Color.WHITE else Color.BLACK

            holder.apply {
                (itemView as MaterialCardView).setCardBackgroundColor(model.color)
                titleTextView.text = model.title
                titleTextView.setTextColor(colorContent)
                checkImageView.apply {
                    visibility = if (model.key == selectedKey) {
                        ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(colorContent))
                        View.VISIBLE
                    } else View.INVISIBLE
                }
            }
        }

    }

    var selectedKey: String? = null

    /**
     * @author Artem Chepurnoy
     */
    class Holder(
        view: View,
        listener: OnItemClickListener<Int>
    ) : AdapterBase.ViewHolderBase(view, listener), View.OnClickListener {

        internal val checkImageView = view.findViewById<ImageView>(R.id.iconImageView)
        internal val titleTextView = view.findViewById<TextView>(R.id.titleTextView)

        init {
            view.setOnClickListener(this)
        }
    }

}