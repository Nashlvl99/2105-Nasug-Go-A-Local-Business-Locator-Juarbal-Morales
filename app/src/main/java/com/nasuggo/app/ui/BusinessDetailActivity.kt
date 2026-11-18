package com.nasuggo.app.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.chip.Chip
import com.nasuggo.app.R
import com.nasuggo.app.data.Business
import com.nasuggo.app.data.BusinessRepository
import com.nasuggo.app.databinding.ActivityBusinessDetailBinding
import com.nasuggo.app.databinding.ItemHoursRowBinding

class BusinessDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBusinessDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityBusinessDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowInsetsControllerCompat(window, binding.root).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = true
        }

        val bar = binding.bottomBar
        val basePaddingBottom = bar.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, 0)
            bar.setPadding(bar.paddingLeft, bar.paddingTop, bar.paddingRight, basePaddingBottom + bars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        val business = intent.getStringExtra(EXTRA_ID)
            ?.let { BusinessRepository.findById(this, it) }
        if (business == null) {
            finish()
            return
        }

        binding.backButton.setOnClickListener { finish() }
        bind(business)
    }

    private fun bind(b: Business) {
        val style = CategoryStyle.of(b.category)
        val open = b.isOpenAt()
        val known = b.hasHours

        binding.detailIconWrap.backgroundTintList = ColorStateList.valueOf(style.color)
        binding.detailIcon.setImageResource(style.icon)
        binding.detailName.text = b.name
        binding.detailCategory.text = b.category
        binding.detailStatus.text = getString(
            when { !known -> R.string.hours_unknown; open -> R.string.open_now; else -> R.string.closed_now }
        )
        binding.detailStatus.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(
                this, when { !known -> R.color.unknown_grey; open -> R.color.open_green; else -> R.color.closed_red }
            )
        )

        if (b.description.isBlank()) {
            binding.cardAbout.visibility = View.GONE
        } else {
            binding.detailDescription.text = b.description
        }

        binding.detailAddress.text = b.address.ifBlank { "-" }
        binding.detailPhone.text = b.phone ?: getString(R.string.no_number)

        val days = listOf(
            "MON" to "Monday", "TUE" to "Tuesday", "WED" to "Wednesday", "THU" to "Thursday",
            "FRI" to "Friday", "SAT" to "Saturday", "SUN" to "Sunday"
        )
        val today = b.todayCode()
        if (!b.hasHours) {
            val row = ItemHoursRowBinding.inflate(layoutInflater, binding.hoursContainer, false)
            row.hoursDay.text = getString(R.string.hours_unknown)
            row.hoursTime.text = ""
            row.hoursDay.setTextColor(Color.parseColor("#9AA7B8"))
            binding.hoursContainer.addView(row.root)
        }
        if (b.hasHours) days.forEach { (code, label) ->
            val row = ItemHoursRowBinding.inflate(layoutInflater, binding.hoursContainer, false)
            val d = b.schedule.forDay(code)
            row.hoursDay.text = label
            row.hoursTime.text =
                if (d?.open != null && d.close != null) "${d.open} – ${d.close}" else "Closed"
            if (code == today) {
                val accent = ContextCompat.getColor(this, R.color.accent_dark)
                row.hoursDay.setTextColor(accent)
                row.hoursTime.setTextColor(accent)
                row.hoursDay.setTypeface(row.hoursDay.typeface, android.graphics.Typeface.BOLD)
                row.hoursTime.setTypeface(row.hoursTime.typeface, android.graphics.Typeface.BOLD)
            } else if (d?.open == null) {
                row.hoursTime.setTextColor(Color.parseColor("#9AA7B8"))
            }
            binding.hoursContainer.addView(row.root)
        }

        if (b.offerings.isEmpty()) {
            binding.cardOfferings.visibility = View.GONE
        } else {
            b.offerings.forEach { text ->
                val chip = layoutInflater.inflate(R.layout.item_offer_chip, binding.offeringsGroup, false) as Chip
                chip.text = text
                binding.offeringsGroup.addView(chip)
            }
        }

        binding.navigateButton.setOnClickListener { Actions.navigate(this, b) }
        val phone = b.phone
        if (phone.isNullOrBlank()) {
            binding.callButton.isEnabled = false
        } else {
            binding.callButton.setOnClickListener { Actions.dial(this, phone) }
        }
    }

    companion object {
        const val EXTRA_ID = "business_id"
    }
}
