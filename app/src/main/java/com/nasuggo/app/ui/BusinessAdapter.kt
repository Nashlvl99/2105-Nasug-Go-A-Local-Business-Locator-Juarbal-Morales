package com.nasuggo.app.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nasuggo.app.R
import com.nasuggo.app.databinding.ItemBusinessBinding

class BusinessAdapter(
    private val dark: Boolean = false,
    private val onClick: (BusinessItem) -> Unit
) : ListAdapter<BusinessItem, BusinessAdapter.VH>(Diff) {
    inner class VH(private val b: ItemBusinessBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: BusinessItem) {
            val ctx = b.root.context
            val biz = item.business
            val style = CategoryStyle.of(biz.category)

            b.name.text = biz.name
            b.category.text = biz.category
            b.address.text = biz.address
            b.distance.text = formatDistance(ctx, item.distanceMeters)
            b.status.text = "• " + ctx.getString(statusRes(item))
            b.status.setTextColor(ContextCompat.getColor(ctx, statusColorRes(item)))

            b.iconWrap.backgroundTintList =
                ColorStateList.valueOf(ColorUtils.setAlphaComponent(style.color, if (dark) 70 else 40))
            b.icon.setImageResource(style.icon)
            b.icon.imageTintList = ColorStateList.valueOf(style.color)

            if (dark) {
                b.root.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.navy_card))
                b.root.strokeColor = ContextCompat.getColor(ctx, R.color.handle)
                b.name.setTextColor(Color.WHITE)
                b.category.setTextColor(ContextCompat.getColor(ctx, R.color.accent))
                b.address.setTextColor(ContextCompat.getColor(ctx, R.color.muted_light))
                b.distance.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#26FFFFFF"))
                b.distance.setTextColor(Color.WHITE)
            } else {
                b.root.setCardBackgroundColor(Color.WHITE)
                b.root.strokeColor = ContextCompat.getColor(ctx, R.color.stroke_light)
                b.name.setTextColor(ContextCompat.getColor(ctx, R.color.on_surface))
                b.category.setTextColor(ContextCompat.getColor(ctx, R.color.accent_dark))
                b.address.setTextColor(ContextCompat.getColor(ctx, R.color.muted))
                b.distance.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E8EEF7"))
                b.distance.setTextColor(ContextCompat.getColor(ctx, R.color.navy))
            }

            b.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemBusinessBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    private object Diff : DiffUtil.ItemCallback<BusinessItem>() {
        override fun areItemsTheSame(a: BusinessItem, b: BusinessItem) = a.business.id == b.business.id
        override fun areContentsTheSame(a: BusinessItem, b: BusinessItem) = a == b
    }
}
