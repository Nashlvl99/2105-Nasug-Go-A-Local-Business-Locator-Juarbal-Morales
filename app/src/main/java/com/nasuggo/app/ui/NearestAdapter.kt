package com.nasuggo.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nasuggo.app.R
import com.nasuggo.app.databinding.ItemNearestBinding

class NearestAdapter(
    private val onOpen: (BusinessItem) -> Unit,
    private val onMap: (BusinessItem) -> Unit
) : RecyclerView.Adapter<NearestAdapter.VH>() {
    private var item: BusinessItem? = null

    fun submit(newItem: BusinessItem?) {
        item = newItem
        notifyItemChanged(0)
    }

    override fun getItemCount(): Int = 1

    inner class VH(private val b: ItemNearestBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(current: BusinessItem?) {
            val ctx = b.root.context
            if (current == null) {
                b.nearestLabel.setText(R.string.nearest_label)
                b.nearestName.text = ctx.getString(R.string.nearest_none)
                b.nearestMeta.visibility = View.GONE
                b.nearestMap.visibility = View.GONE
                b.nearestIcon.setImageResource(R.drawable.ic_near_me)
                b.nearestCard.isClickable = false
                b.nearestCard.setOnClickListener(null)
            } else {
                val style = CategoryStyle.of(current.business.category)
                b.nearestLabel.setText(if (current.openNow) R.string.nearest_label else R.string.nearest_place_label)
                b.nearestName.text = current.business.name
                b.nearestMeta.visibility = View.VISIBLE
                b.nearestMeta.text = ctx.getString(
                    R.string.nearest_meta_format,
                    current.business.category,
                    formatDistance(ctx, current.distanceMeters)
                )
                b.nearestMap.visibility = View.VISIBLE
                b.nearestIcon.setImageResource(style.icon)
                b.nearestCard.isClickable = true
                b.nearestCard.setOnClickListener { onOpen(current) }
                b.nearestMap.setOnClickListener { onMap(current) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemNearestBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(item)
}
