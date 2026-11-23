package com.nasuggo.app.ui

import android.content.Context
import com.nasuggo.app.R

fun formatDistance(context: Context, meters: Float): String =
    if (meters < 1000f) context.getString(R.string.distance_m, meters.toInt())
    else context.getString(R.string.distance_km, meters / 1000f)
