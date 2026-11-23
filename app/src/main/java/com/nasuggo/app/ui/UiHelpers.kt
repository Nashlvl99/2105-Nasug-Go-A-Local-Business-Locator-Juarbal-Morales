package com.nasuggo.app.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.LayoutRes
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

fun setupCategoryChips(
    group: ChipGroup,
    inflater: LayoutInflater,
    @LayoutRes chipLayout: Int,
    vm: SharedViewModel,
    allLabel: String
) {
    group.removeAllViews()

    fun add(label: String, category: String?) {
        val chip = inflater.inflate(chipLayout, group, false) as Chip
        chip.id = View.generateViewId()
        chip.text = label
        chip.isChecked = vm.currentCategory == category
        chip.setOnClickListener { vm.setCategory(category) }
        group.addView(chip)
    }

    add(allLabel, null)
    vm.categories.forEach { add(it, it) }
}

fun bindSearchField(edit: EditText, clear: View, vm: SharedViewModel) {
    edit.setText(vm.currentQuery)
    clear.visibility = if (vm.currentQuery.isEmpty()) View.GONE else View.VISIBLE

    edit.doAfterTextChanged { text ->
        val s = text?.toString().orEmpty()
        clear.visibility = if (s.isEmpty()) View.GONE else View.VISIBLE
        vm.setQuery(s)
    }
    clear.setOnClickListener { edit.setText("") }
    edit.setOnEditorActionListener { v, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
            v.clearFocus()
            true
        } else {
            false
        }
    }
}

fun setupBarangayChips(
    group: ChipGroup,
    inflater: LayoutInflater,
    @LayoutRes chipLayout: Int,
    vm: SharedViewModel,
    allLabel: String
) {
    group.removeAllViews()

    fun add(label: String, barangay: String?) {
        val chip = inflater.inflate(chipLayout, group, false) as Chip
        chip.id = View.generateViewId()
        chip.text = label
        chip.isChecked = vm.currentBarangay == barangay
        chip.setOnClickListener { vm.setBarangay(barangay) }
        group.addView(chip)
    }

    add(allLabel, null)
    vm.barangays.forEach { add(it, it) }
}

fun setupSortChips(
    group: ChipGroup,
    inflater: LayoutInflater,
    @LayoutRes chipLayout: Int,
    vm: SharedViewModel,
    prefixFormat: (String) -> String
) {
    group.removeAllViews()
    SortOption.values().forEach { option ->
        val chip = inflater.inflate(chipLayout, group, false) as Chip
        chip.id = View.generateViewId()
        chip.text = prefixFormat(option.label)
        chip.isChecked = vm.currentSort == option
        chip.setOnClickListener { vm.setSort(option) }
        group.addView(chip)
    }
}
