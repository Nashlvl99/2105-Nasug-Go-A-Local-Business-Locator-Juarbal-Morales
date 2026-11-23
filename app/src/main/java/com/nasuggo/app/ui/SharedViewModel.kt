package com.nasuggo.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nasuggo.app.data.Business
import com.nasuggo.app.data.BusinessFilter
import com.nasuggo.app.data.BusinessGroup
import com.nasuggo.app.data.BusinessRepository
import com.nasuggo.app.data.BusinessSource
import com.nasuggo.app.data.BarangayNames
import com.nasuggo.app.data.LocationUtils

data class UserFix(val lat: Double, val lng: Double, val accuracyM: Float)

data class BusinessItem(
    val business: Business,
    val distanceMeters: Float
)

class SharedViewModel(app: Application) : AndroidViewModel(app) {
    private val source: BusinessSource = BusinessRepository.source(app)
    private val all: List<Business> = source.getAll()

    val categories: List<String> =
        BusinessGroup.values().filter { g -> all.any { it.group == g } }.map { it.label }

    val barangays: List<String> =
        all.map { it.barangay }.filter { it.isNotBlank() }.distinct().sortedBy { BarangayNames.sortKey(it) }

    private var filter = BusinessFilter()
    private var sort: SortOption = SortOption.Nearest

    val currentBarangay: String? get() = filter.barangay
    val currentSort: SortOption get() = sort

    val currentQuery: String get() = filter.query

    val currentCategory: String? get() = filter.group?.label

    private var userLat = LocationUtils.DEFAULT_LAT
    private var userLng = LocationUtils.DEFAULT_LNG

    var hasRealLocation = false
        private set

    var outsideArea = false
        private set

    var darkMap = true

    val currentLat get() = userLat
    val currentLng get() = userLng

    private val _userFix = MutableLiveData<UserFix?>()
    val userFix: LiveData<UserFix?> = _userFix

    private val _items = MutableLiveData<List<BusinessItem>>()
    val items: LiveData<List<BusinessItem>> = _items

    private val _nearest = MutableLiveData<BusinessItem?>()
    val nearest: LiveData<BusinessItem?> = _nearest

    private val _mapFocus = MutableLiveData<String?>()
    val mapFocus: LiveData<String?> = _mapFocus

    init {
        refresh()
    }

    fun setQuery(text: String) {
        val trimmed = text.trim()
        if (trimmed == filter.query) return
        filter = filter.copy(query = trimmed)
        refresh()
    }

    fun setCategory(value: String?) {
        val group = BusinessGroup.fromLabel(value)
        if (group == filter.group) return
        filter = filter.copy(group = group)
        refresh()
    }

    fun setBarangay(value: String?) {
        if (value == filter.barangay) return
        filter = filter.copy(barangay = value)
        refresh()
    }

    fun setSort(option: SortOption) {
        if (option == sort) return
        sort = option
        refresh()
    }

    fun setUserLocation(lat: Double, lng: Double, accuracyM: Float = 0f) {
        val prev = _userFix.value
        if (prev != null && accuracyM > MAX_USEFUL_ACCURACY_M && prev.accuracyM <= MAX_USEFUL_ACCURACY_M) return
        _userFix.value = UserFix(lat, lng, accuracyM)

        val away = LocationUtils.distanceMeters(
            LocationUtils.DEFAULT_LAT, LocationUtils.DEFAULT_LNG, lat, lng
        )
        val far = away > MAX_LOCAL_DISTANCE_M
        val newLat = if (far) LocationUtils.DEFAULT_LAT else lat
        val newLng = if (far) LocationUtils.DEFAULT_LNG else lng
        val moved = LocationUtils.distanceMeters(userLat, userLng, newLat, newLng) > MIN_MOVE_M
        val modeChanged = hasRealLocation == far

        outsideArea = far
        hasRealLocation = !far
        userLat = newLat
        userLng = newLng
        if (moved || modeChanged) refresh()
    }

    fun requestMapFocus(id: String) {
        _mapFocus.value = id
    }

    fun consumeMapFocus() {
        _mapFocus.value = null
    }

    private fun refresh() {
        val list = all.filter { filter.accepts(it) }
            .map {
                BusinessItem(
                    business = it,
                    distanceMeters = LocationUtils.distanceMeters(
                        userLat, userLng, it.latitude, it.longitude
                    )
                )
            }
            .sortedWith(sort.comparator())

        _items.value = list

        _nearest.value = list.minByOrNull { it.distanceMeters }
    }

    private companion object {
        const val MAX_LOCAL_DISTANCE_M = 30_000f
        const val MAX_USEFUL_ACCURACY_M = 100f
        const val MIN_MOVE_M = 8f
    }
}
