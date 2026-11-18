package com.nasuggo.app.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.core.content.ContextCompat

object MarkerIcons {
    private val closedColor = Color.parseColor("#5B6B85")

    fun name(category: String, open: Boolean, selected: Boolean): String =
        "pin|${category.lowercase()}|${if (open) "open" else "closed"}|${if (selected) "sel" else "norm"}"

    fun bitmap(ctx: Context, category: String, open: Boolean, selected: Boolean): Bitmap {
        val d = ctx.resources.displayMetrics.density
        val style = CategoryStyle.of(category)

        val diameter = (if (selected) 54f else 36f) * d
        val tip = 9f * d
        val w = diameter.toInt() + 2
        val h = (diameter + tip).toInt() + 2

        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        bmp.density = ctx.resources.displayMetrics.densityDpi

        val c = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val cx = w / 2f
        val r = diameter / 2f
        val cy = r + 1f

        paint.color = Color.WHITE
        val pointer = Path().apply {
            moveTo(cx - 7f * d, cy + r - 3f * d)
            lineTo(cx + 7f * d, cy + r - 3f * d)
            lineTo(cx, cy + r + tip - 1f)
            close()
        }
        c.drawPath(pointer, paint)

        c.drawCircle(cx, cy, r, paint)
        paint.color = if (open) style.color else closedColor
        c.drawCircle(cx, cy, r - 3f * d, paint)

        ContextCompat.getDrawable(ctx, style.icon)?.mutate()?.let { icon ->
            icon.setTint(Color.WHITE)
            val half = r * 0.52f
            icon.setBounds(
                (cx - half).toInt(), (cy - half).toInt(),
                (cx + half).toInt(), (cy + half).toInt()
            )
            icon.draw(c)
        }
        return bmp
    }
}
