package com.nasuggo.app.ui

import android.graphics.Color
import androidx.annotation.DrawableRes
import com.nasuggo.app.R

object CategoryStyle {
    data class Style(val color: Int, @DrawableRes val icon: Int, val keepColors: Boolean = false)

    private val fallback = Style(Color.parseColor("#19C3B1"), R.drawable.ic_cat_place)

    fun of(category: String): Style = when (category.trim().lowercase()) {
        "dining", "food", "restaurants" -> Style(Color.parseColor("#FF8A3D"), R.drawable.ic_cat_dining)
        "hardware" -> Style(Color.parseColor("#F5B400"), R.drawable.ic_cat_hardware)
        "pharmacies", "pharmacy" -> Style(Color.parseColor("#2BC48A"), R.drawable.ic_cat_pharmacy)
        "services" -> Style(Color.parseColor("#4DA3FF"), R.drawable.ic_cat_services)
        "groceries", "grocery" -> Style(Color.parseColor("#B07CFF"), R.drawable.ic_cat_grocery)
        "shopping" -> Style(Color.parseColor("#F06292"), R.drawable.ic_cat_shopping)
        "fitness" -> Style(Color.parseColor("#26C6DA"), R.drawable.ic_cat_fitness)
        "entertainment" -> Style(Color.parseColor("#7C6CF0"), R.drawable.ic_cat_games)
        "health" -> Style(Color.parseColor("#EF5350"), R.drawable.ic_cat_health)

        "racket sports" -> Style(Color.parseColor("#5C6BC0"), R.drawable.ic_cat_racket)
        "dental" -> Style(Color.parseColor("#00ACC1"), R.drawable.ic_cat_dental)
        "rice supply" -> Style(Color.parseColor("#A1887F"), R.drawable.ic_cat_rice)
        "animal clinic" -> Style(Color.parseColor("#E53935"), R.drawable.ic_cat_animal_clinic)
        "pet shop" -> Style(Color.parseColor("#EC407A"), R.drawable.ic_cat_pet)
        "gas station" -> Style(Color.parseColor("#1E88E5"), R.drawable.ic_cat_gas)
        "bakery" -> Style(Color.parseColor("#FFA726"), R.drawable.ic_cat_bakery)
        "gadgets" -> Style(Color.parseColor("#78909C"), R.drawable.ic_cat_gadgets)
        "wellness & spa" -> Style(Color.parseColor("#BA68C8"), R.drawable.ic_cat_spa)
        "printing" -> Style(Color.parseColor("#9575CD"), R.drawable.ic_cat_print)
        "bank" -> Style(Color.parseColor("#7CB342"), R.drawable.ic_cat_bank)
        "beauty" -> Style(Color.parseColor("#D81B60"), R.drawable.ic_cat_beauty)
        "aesthetics" -> Style(Color.parseColor("#673AB7"), R.drawable.ic_cat_aesthetics)
        "vehicle" -> Style(Color.parseColor("#546E7A"), R.drawable.ic_cat_vehicle)
        "motorcycle" -> Style(Color.parseColor("#37474F"), R.drawable.ic_cat_motorcycle)
        "appliance" -> Style(Color.parseColor("#00897B"), R.drawable.ic_cat_appliance)
        "plant and flowers" -> Style(Color.parseColor("#66BB6A"), R.drawable.ic_cat_florist)
        "vape" -> Style(Color.parseColor("#9E9D24"), R.drawable.ic_cat_vape)
        "school supplies" -> Style(Color.parseColor("#F9A825"), R.drawable.ic_cat_school)
        "outfits" -> Style(Color.parseColor("#AB47BC"), R.drawable.ic_cat_outfit)
        "electronics" -> Style(Color.parseColor("#3F51B5"), R.drawable.ic_cat_electronics)
        "sports" -> Style(Color.parseColor("#FB8C00"), R.drawable.ic_cat_sports)
        "party needs" -> Style(Color.parseColor("#FF4081"), R.drawable.ic_cat_party)
        "meat" -> Style(Color.parseColor("#D84315"), R.drawable.ic_cat_meat)
        "coffee" -> Style(Color.parseColor("#8D6E63"), R.drawable.ic_cat_coffee)
        "footwear" -> Style(Color.parseColor("#6D4C41"), R.drawable.ic_cat_footwear)
        "soap" -> Style(Color.parseColor("#4DD0E1"), R.drawable.ic_cat_soap)
        "jollibee - fast food" -> Style(Color.parseColor("#FFFFFF"), R.drawable.jollibee, keepColors = true)
        "mcdonalds - fast food" -> Style(Color.parseColor("#FFFFFF"), R.drawable.mcdonalds, keepColors = true)
        "mang inasal - fast food" -> Style(Color.parseColor("#FFFFFF"), R.drawable.manginasal, keepColors = true)
        "alfamart - convenience store" -> Style(Color.parseColor("#FFFFFF"), R.drawable.alfamart, keepColors = true)
        "savemore - grocery and supermarket" -> Style(Color.parseColor("#FFFFFF"), R.drawable.savemore, keepColors = true)
        "dali - convenience store" -> Style(Color.parseColor("#FFFFFF"), R.drawable.dali, keepColors = true)
        "petron - gas station" -> Style(Color.parseColor("#FFFFFF"), R.drawable.petron, keepColors = true)
        "beverage" -> Style(Color.parseColor("#03A9F4"), R.drawable.ic_cat_beverage)
        else -> fallback
    }
}