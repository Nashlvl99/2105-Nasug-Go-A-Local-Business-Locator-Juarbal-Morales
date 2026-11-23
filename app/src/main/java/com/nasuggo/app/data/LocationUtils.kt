package com.nasuggo.app.data

import android.location.Location

object LocationUtils {
    const val DEFAULT_LAT = 14.0722
    const val DEFAULT_LNG = 120.6330

    fun distanceMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
        val out = FloatArray(1)
        Location.distanceBetween(lat1, lng1, lat2, lng2, out)
        return out[0]
    }
}
