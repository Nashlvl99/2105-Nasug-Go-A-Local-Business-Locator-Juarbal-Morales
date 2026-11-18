package com.nasuggo.app.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.nasuggo.app.R
import com.nasuggo.app.data.Business

object Actions {
    fun navigate(ctx: Context, b: Business) {
        val navUri = Uri.parse("google.navigation:q=${b.latitude},${b.longitude}&mode=w")
        val googleMaps = Intent(Intent.ACTION_VIEW, navUri).setPackage("com.google.android.apps.maps")
        try {
            ctx.startActivity(googleMaps)
        } catch (e: ActivityNotFoundException) {
            val geo = Uri.parse(
                "geo:${b.latitude},${b.longitude}?q=${b.latitude},${b.longitude}(${Uri.encode(b.name)})"
            )
            try {
                ctx.startActivity(Intent(Intent.ACTION_VIEW, geo))
            } catch (e2: ActivityNotFoundException) {
                Toast.makeText(ctx, R.string.no_maps_app, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun dial(ctx: Context, phone: String) {
        try {
            ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(ctx, R.string.no_dialer, Toast.LENGTH_SHORT).show()
        }
    }

    fun openDetail(ctx: Context, businessId: String) {
        ctx.startActivity(
            Intent(ctx, BusinessDetailActivity::class.java)
                .putExtra(BusinessDetailActivity.EXTRA_ID, businessId)
        )
    }
}
