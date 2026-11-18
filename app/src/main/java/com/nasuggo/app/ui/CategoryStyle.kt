package com.nasuggo.app.ui

import android.graphics.Color
import androidx.annotation.DrawableRes
import com.nasuggo.app.R

object CategoryStyle {
    data class Style(val color: Int, @DrawableRes val icon: Int)

    private val fallback = Style(Color.parseColor("#19C3B1"), R.drawable.ic_cat_place)

    fun of(category: String): Style = when (category.trim().lowercase()) {
        "dining", "food", "restaurants" -> Style(Color.parseColor("#FF8A3D"), R.drawable.ic_cat_dining)
        "hardware" -> Style(Color.parseColor("#F5B400"), R.drawable.ic_cat_hardware)
        "pharmacies", "pharmacy" -> Style(Color.parseColor("#2BC48A"), R.drawable.ic_cat_pharmacy)
        "services" -> Style(Color.parseColor("#4DA3FF"), R.drawable.ic_cat_services)
        "groceries", "grocery" -> Style(Color.parseColor("#B07CFF"), R.drawable.ic_cat_grocery)
        "shopping" -> Style(Color.parseColor("#F06292"), R.drawable.ic_cat_shopping)
        "beauty & fitness" -> Style(Color.parseColor("#26C6DA"), R.drawable.ic_cat_fitness)
        "entertainment" -> Style(Color.parseColor("#7C6CF0"), R.drawable.ic_cat_games)
        "health" -> Style(Color.parseColor("#EF5350"), R.drawable.ic_cat_health)
        else -> fallback
    }
}
