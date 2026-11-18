package com.nasuggo.app.ui

import android.content.Context
import com.nasuggo.app.R

fun formatDistance(context: Context, meters: Float): String =
    if (meters < 1000f) context.getString(R.string.distance_m, meters.toInt())
    else context.getString(R.string.distance_km, meters / 1000f)

fun statusRes(item: BusinessItem): Int = when {
    !item.hoursKnown -> R.string.hours_unknown
    item.openNow -> R.string.open_now
    else -> R.string.closed_now
}

fun statusColorRes(item: BusinessItem): Int = when {
    !item.hoursKnown -> R.color.unknown_grey
    item.openNow -> R.color.open_green
    else -> R.color.closed_red
}
