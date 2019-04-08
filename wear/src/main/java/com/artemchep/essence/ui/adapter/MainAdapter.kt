package com.artemchep.essence.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.artemchep.essence.R
import com.artemchep.essence.extensions.setTextExclusive
import com.artemchep.essence.ui.adapters.AdapterBase
import com.artemchep.essence.ui.interfaces.OnItemClickListener
import com.artemchep.essence.ui.model.ConfigItem

/**
 * @author Artem Chepurnoy
 */
open class MainAdapter(
    models: MutableList<ConfigItem>,
    title: CharSequence?
) : AdapterTitled<ConfigItem, MainAdapter.Holder>(models, title) {

    override val binderItem = object : Binder<Holder>() {

        override fun createView(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): View {
            return inflater.inflate(R.layout.item_config, parent, false)
        }

        override fun createViewHolder(itemView: View, viewType: Int): Holder {
            return Holder(itemView, this@MainAdapter)
        }

        override fun bindViewHolder(holder: Holder, position: Int) {
            val model = getItem(position)
            holder.apply {
                titleTextView.text = model.title
                summaryTextView.setTextExclusive(model.summary)
                iconImageView.setImageDrawable(model.icon)
            }
        }

    }

    /**
     * @author Artem Chepurnoy
     */
    class Holder(
        view: View,
        listener: OnItemClickListener<Int>
    ) : AdapterBase.ViewHolderBase(view, listener), View.OnClickListener {

        internal val iconImageView = view.findViewById<ImageView>(R.id.iconImageView)
        internal val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
        internal val summaryTextView = view.findViewById<TextView>(R.id.summaryTextView)

        init {
            view.setOnClickListener(this)
        }
    }

}