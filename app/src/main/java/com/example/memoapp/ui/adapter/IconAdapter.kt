package com.example.memoapp.ui.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.example.memoapp.R
import com.example.memoapp.model.CardState
import com.example.memoapp.model.IconModel

class IconAdapter(
    private val context: Context,
    private val checkIsMatchFound: (clickedItem: IconModel) -> Unit
): BaseAdapter() {

    private var icons: MutableList<IconModel> = mutableListOf()

    override fun getCount(): Int {
        return icons.size
    }

    override fun getItemId(position: Int): Long {
        return icons[position].id.leastSignificantBits
    }

    override fun getItem(position: Int): Any {
        return icons[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.layout_icon,
            parent,
            false
        )

        val item = icons[position]

        view.findViewById<ImageView>(R.id.ivIcon)?.apply {
            this.background = flipCard(item.res, item.state)

            setOnClickListener{
                if (item.state == CardState.CLOSED) {
                    item.state == CardState.OPEN
                    this.background = flipCard(item.res, item.state)
                    checkIsMatchFound(item)
                }
            }
        }

        return view
    }

    fun updateData(randomIcons: List<IconModel>) {
        icons.run {
            clear()
            addAll(randomIcons)
        }
        notifyDataSetChanged()
    }

    fun pairMatch(lastOpenedCard: IconModel?, clickedItem: IconModel?) {
        icons.run {
            find {it.id == lastOpenedCard?.id}?.state = CardState.PAIRED
            find {it.id == clickedItem?.id}?.state = CardState.PAIRED
        }
        notifyDataSetChanged()
    }

    fun revertVisibility(lastOpenedCard: IconModel?, clickedItem: IconModel?) {
        icons.run {
            find {it.id == lastOpenedCard?.id}?.state = CardState.CLOSED
            find {it.id == clickedItem?.id}?.state = CardState.CLOSED
        }
        notifyDataSetChanged()
    }

    private fun flipCard(@DrawableRes res: Int, state: CardState): Drawable? {
        val src = when (state) {
            CardState.OPEN -> res
            CardState.CLOSED -> R.drawable.ic_card_background
            CardState.PAIRED -> android.R.color.transparent
        }
        return ContextCompat.getDrawable(context, src)
    }
}