package com.nasuggo.app.ui

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.PointF
import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.nasuggo.app.R
import com.nasuggo.app.data.LocationUtils
import com.nasuggo.app.databinding.FragmentMapBinding
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import kotlin.math.hypot
import kotlin.math.max

class MapFragment : Fragment() {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val vm: SharedViewModel by activityViewModels()

    private var map: MapLibreMap? = null
    private var style: Style? = null
    private val addedImages = HashSet<String>()

    private var selectedId: String? = null
    private var pendingLocate = false
    private var styleAttempt = 0
    private var styleToken = 0

    private lateinit var behavior: BottomSheetBehavior<View>
    private lateinit var listAdapter: BusinessAdapter

    private var pulse: ValueAnimator? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        MapLibre.getInstance(requireContext())
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = requireContext()

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.addOnDidFailLoadingMapListener { onStyleFailed() }
        binding.mapView.getMapAsync { onMapReady(it) }

        behavior = BottomSheetBehavior.from(binding.sheet)
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val b = _binding ?: return
                val a = (1f - slideOffset * 2.5f).coerceIn(0f, 1f)
                b.fabColumn.alpha = a
                b.fabColumn.visibility = if (a <= 0f) View.INVISIBLE else View.VISIBLE
            }
        })

        listAdapter = BusinessAdapter(dark = true) { select(it.business.id) }
        binding.mapList.layoutManager = LinearLayoutManager(ctx)
        binding.mapList.adapter = listAdapter

        setupCategoryChips(
            binding.mapChipGroup, layoutInflater, R.layout.item_chip_map, vm,
            getString(R.string.category_all)
        )
        bindSearchField(binding.mapSearchInput, binding.mapSearchClear, vm)

        // Dark map only: the style toggle button is hidden.
        binding.fabStyle.visibility = View.GONE
        binding.fabNearest.setOnClickListener {
            val nearest = vm.nearest.value
            if (nearest == null) {
                Toast.makeText(ctx, R.string.nearest_none, Toast.LENGTH_SHORT).show()
            } else {
                select(nearest.business.id)
            }
        }
        binding.fabLocate.setOnClickListener { locate() }
        binding.detailClose.setOnClickListener { deselect() }
        binding.detailNavigate.setOnClickListener { selectedItem()?.let { Actions.navigate(ctx, it.business) } }
        binding.detailDetails.setOnClickListener { selectedItem()?.let { Actions.openDetail(ctx, it.business.id) } }

        vm.items.observe(viewLifecycleOwner) { items ->
            updateSheetHeader(items)
            listAdapter.submitList(items)
            refreshPins()
            refreshUserDot()

            val id = selectedId
            if (id != null) {
                val still = items.firstOrNull { it.business.id == id }
                if (still == null) deselect() else showDetail(still, animateSheet = false)
            }
        }
        vm.userFix.observe(viewLifecycleOwner) { fix ->
            refreshUserDot()
            if (pendingLocate && fix != null) {
                pendingLocate = false
                moveToUser()
            }
        }
        vm.mapFocus.observe(viewLifecycleOwner) { handleFocus() }
    }

    private fun onMapReady(m: MapLibreMap) {
        if (_binding == null) return
        map = m

        m.uiSettings.isTiltGesturesEnabled = false
        m.uiSettings.isCompassEnabled = true
        m.setMinZoomPreference(14.0)
        m.setMaxZoomPreference(20.0)
        m.setLatLngBoundsForCameraTarget(AREA_BOUNDS)
        m.cameraPosition = CameraPosition.Builder()
            .target(LatLng(LocationUtils.DEFAULT_LAT, LocationUtils.DEFAULT_LNG))
            .zoom(16.2)
            .build()

        m.addOnMapClickListener { latLng -> onMapClick(latLng) }

        applyMapPadding(null)
        loadStyle()
        handleFocus()
    }

    private fun styleUrls(): List<String> = DARK_STYLES

    private fun loadStyle() {
        val m = map ?: return
        val urls = styleUrls()
        val url = urls[styleAttempt.coerceIn(0, urls.lastIndex)]
        pulse?.cancel()
        pulse = null
        style = null
        val token = ++styleToken
        m.setStyle(Style.Builder().fromUri(url)) { st ->
            if (token == styleToken && _binding != null) onStyleLoaded(st)
        }
    }

    private fun onStyleFailed() {
        val b = _binding ?: return
        if (style != null) return
        val urls = styleUrls()
        if (styleAttempt < urls.lastIndex) {
            styleAttempt++
            loadStyle()
        } else {
            b.mapNotice.visibility = View.VISIBLE
        }
    }

    private fun onStyleLoaded(st: Style) {
        val b = _binding ?: return
        val ctx = context ?: return
        style = st
        addedImages.clear()
        b.mapNotice.visibility = View.GONE

        val d = ctx.resources.displayMetrics.density

        st.addSource(GeoJsonSource(SRC_PINS, FeatureCollection.fromFeatures(emptyList<Feature>())))
        st.addLayer(
            SymbolLayer(LAYER_PINS, SRC_PINS).withProperties(
                PropertyFactory.iconImage(Expression.get("icon")),
                PropertyFactory.iconAnchor(Property.ICON_ANCHOR_BOTTOM),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconIgnorePlacement(true),
                PropertyFactory.symbolZOrder(Property.SYMBOL_Z_ORDER_SOURCE)
            )
        )

        st.addSource(GeoJsonSource(SRC_USER, FeatureCollection.fromFeatures(emptyList<Feature>())))
        st.addLayer(
            CircleLayer(LAYER_USER_ACCURACY, SRC_USER).withProperties(
                PropertyFactory.circleRadius(0f),
                PropertyFactory.circleColor(USER_BLUE),
                PropertyFactory.circleOpacity(0.16f),
                PropertyFactory.circleStrokeColor(USER_BLUE),
                PropertyFactory.circleStrokeWidth(1f),
                PropertyFactory.circleStrokeOpacity(0.5f)
            )
        )
        st.addLayer(
            CircleLayer(LAYER_USER_HALO, SRC_USER).withProperties(
                PropertyFactory.circleRadius(16f * d),
                PropertyFactory.circleColor(USER_BLUE),
                PropertyFactory.circleOpacity(0.35f)
            )
        )
        st.addLayer(
            CircleLayer(LAYER_USER_DOT, SRC_USER).withProperties(
                PropertyFactory.circleRadius(9f * d),
                PropertyFactory.circleColor(USER_BLUE),
                PropertyFactory.circleStrokeColor("#FFFFFF"),
                PropertyFactory.circleStrokeWidth(3f * d)
            )
        )

        refreshPins()
        refreshUserDot()
        startPulse(st, d)
    }

    private fun startPulse(st: Style, d: Float) {
        pulse?.cancel()
        pulse = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1800L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { anim ->
                if (style !== st) {
                    anim.cancel()
                    return@addUpdateListener
                }
                val f = anim.animatedValue as Float
                try {
                    st.getLayerAs<CircleLayer>(LAYER_USER_HALO)?.setProperties(
                        PropertyFactory.circleRadius((12f + 22f * f) * d),
                        PropertyFactory.circleOpacity(0.4f * (1f - f))
                    )
                } catch (e: IllegalStateException) {
                    anim.cancel()
                }
            }
            start()
        }
    }

    private fun applyMapPadding(sheetPeekPx: Int?) {
        val ctx = context ?: return
        val m = map ?: return
        val bottom = sheetPeekPx ?: ctx.dp(DEFAULT_PEEK_DP)
        m.setPadding(0, ctx.dp(132), 0, bottom)

        m.uiSettings.setLogoMargins(ctx.dp(8), 0, 0, bottom + ctx.dp(8))
        m.uiSettings.setAttributionMargins(ctx.dp(96), 0, 0, bottom + ctx.dp(8))
        m.uiSettings.setCompassMargins(0, ctx.dp(140), ctx.dp(12), 0)
    }

    private fun zFor(selected: Boolean): Int = if (selected) 2 else 1

    private fun refreshPins() {
        val st = style ?: return
        val ctx = context ?: return
        val items = vm.items.value ?: return

        val ordered = items.sortedBy { zFor(it.business.id == selectedId) }
        val features = ordered.map { item ->
            val b = item.business
            val selected = b.id == selectedId
            val icon = MarkerIcons.name(b.category, selected)
            if (addedImages.add(icon)) {
                st.addImage(icon, MarkerIcons.bitmap(ctx, b.category, selected))
            }
            Feature.fromGeometry(Point.fromLngLat(b.longitude, b.latitude)).also {
                it.addStringProperty("id", b.id)
                it.addStringProperty("icon", icon)
            }
        }
        st.getSourceAs<GeoJsonSource>(SRC_PINS)?.setGeoJson(FeatureCollection.fromFeatures(features))
    }

    private fun refreshUserDot() {
        val st = style ?: return
        val fix = vm.userFix.value
        val features = if (fix != null) {
            listOf(Feature.fromGeometry(Point.fromLngLat(fix.lng, fix.lat)))
        } else {
            emptyList()
        }
        st.getSourceAs<GeoJsonSource>(SRC_USER)?.setGeoJson(FeatureCollection.fromFeatures(features))

        val radius = if (fix != null && fix.accuracyM > 0f) {
            val metersPerPxAtZoom0 = 78271.517 * Math.cos(Math.toRadians(fix.lat))
            val r0 = fix.accuracyM.coerceIn(3f, 500f) / metersPerPxAtZoom0
            Expression.interpolate(
                Expression.exponential(2f), Expression.zoom(),
                Expression.stop(0, Expression.literal(r0)),
                Expression.stop(22, Expression.literal(r0 * 4194304.0))
            )
        } else {
            Expression.literal(0f)
        }
        st.getLayerAs<CircleLayer>(LAYER_USER_ACCURACY)?.setProperties(PropertyFactory.circleRadius(radius))
    }

    private fun onMapClick(latLng: LatLng): Boolean {
        val m = map ?: return false
        val ctx = context ?: return false

        val tap = m.projection.toScreenLocation(latLng)
        val r = ctx.dp(24).toFloat()
        val hits = m.queryRenderedFeatures(RectF(tap.x - r, tap.y - r, tap.x + r, tap.y + r), LAYER_PINS)

        val discLift = ctx.dp(22).toFloat()
        val best = hits.minByOrNull { f ->
            val p = f.geometry() as? Point ?: return@minByOrNull Float.MAX_VALUE
            val s: PointF = m.projection.toScreenLocation(LatLng(p.latitude(), p.longitude()))
            hypot(s.x - tap.x, (s.y - discLift) - tap.y)
        }
        val id = best?.getStringProperty("id")
        if (id != null) select(id) else deselect()
        return true
    }

    private fun selectedItem(): BusinessItem? =
        vm.items.value?.firstOrNull { it.business.id == selectedId }

    private fun select(id: String) {
        val item = vm.items.value?.firstOrNull { it.business.id == id } ?: return
        selectedId = id
        refreshPins()
        showDetail(item, animateSheet = true)

        map?.let { m ->
            val target = LatLng(item.business.latitude, item.business.longitude)
            m.animateCamera(
                CameraUpdateFactory.newLatLngZoom(target, max(m.cameraPosition.zoom, 16.5))
            )
        }
    }

    private fun deselect() {
        if (selectedId == null) return
        selectedId = null
        refreshPins()

        val b = _binding ?: return
        val ctx = requireContext()
        b.detailPanel.visibility = View.GONE
        b.listPanel.visibility = View.VISIBLE
        behavior.isDraggable = true
        behavior.setPeekHeight(ctx.dp(DEFAULT_PEEK_DP), true)
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        b.fabColumn.animate().translationY(0f).start()
        applyMapPadding(null)
    }

    private fun showDetail(item: BusinessItem, animateSheet: Boolean) {
        val b = _binding ?: return
        val ctx = requireContext()
        val biz = item.business
        val cat = CategoryStyle.of(biz.category)

        b.detailName.text = biz.name
        b.detailCategory.text = biz.category
        b.detailMeta.text = formatDistance(ctx, item.distanceMeters)
        b.detailAddress.text = biz.address
        b.detailIconWrap.backgroundTintList = ColorStateList.valueOf(cat.color)
        b.detailIcon.setImageResource(cat.icon)

        b.listPanel.visibility = View.GONE
        b.detailPanel.visibility = View.VISIBLE
        behavior.isDraggable = false

        b.detailPanel.measure(
            View.MeasureSpec.makeMeasureSpec(resources.displayMetrics.widthPixels, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val peek = b.detailPanel.measuredHeight + ctx.dp(HANDLE_DP)
        behavior.setPeekHeight(peek, animateSheet)
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED

        val lift = (peek - ctx.dp(DEFAULT_PEEK_DP)).toFloat()
        if (animateSheet) b.fabColumn.animate().translationY(-lift).start() else b.fabColumn.translationY = -lift
        applyMapPadding(peek)
    }

    private fun updateSheetHeader(items: List<BusinessItem>) {
        val b = _binding ?: return
        b.sheetTitle.text = resources.getQuantityString(R.plurals.place_count, items.size, items.size)
        val nearest = vm.nearest.value
        b.sheetSubtitle.text = if (nearest == null) {
            getString(R.string.sheet_none)
        } else {
            getString(
                R.string.sheet_nearest,
                nearest.business.name,
                formatDistance(requireContext(), nearest.distanceMeters)
            )
        }
    }

    private fun handleFocus() {
        val id = vm.mapFocus.value ?: return
        if (map == null) return
        select(id)
        vm.consumeMapFocus()
    }

    private fun locate() {
        val act = activity as? MainActivity ?: return
        if (vm.userFix.value == null) {
            pendingLocate = true
            act.requestLocation()
        } else {
            moveToUser()
        }
    }

    private fun moveToUser() {
        val m = map ?: return
        val ctx = context ?: return
        val fix = vm.userFix.value
        if (fix == null) {
            m.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(vm.currentLat, vm.currentLng), 17.0))
            return
        }
        if (vm.outsideArea) {
            m.setLatLngBoundsForCameraTarget(null)
            val away = LocationUtils.distanceMeters(
                LocationUtils.DEFAULT_LAT, LocationUtils.DEFAULT_LNG, fix.lat, fix.lng
            )
            Toast.makeText(ctx, getString(R.string.location_far, formatDistance(ctx, away)), Toast.LENGTH_LONG).show()
        }
        m.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(fix.lat, fix.lng), 17.5))
    }

    override fun onStart() {
        super.onStart()
        _binding?.mapView?.onStart()
        val st = style
        val d = context?.resources?.displayMetrics?.density
        if (st != null && d != null) startPulse(st, d)
    }

    override fun onResume() {
        super.onResume()
        _binding?.mapView?.onResume()
    }

    override fun onPause() {
        _binding?.mapView?.onPause()
        super.onPause()
    }

    override fun onStop() {
        pulse?.cancel()
        pulse = null
        _binding?.mapView?.onStop()
        super.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        _binding?.mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        _binding?.mapView?.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        pulse?.cancel()
        pulse = null
        _binding?.mapList?.adapter = null
        _binding?.mapView?.onDestroy()
        map = null
        style = null
        addedImages.clear()
        _binding = null
        super.onDestroyView()
    }

    private companion object {
        const val DEFAULT_PEEK_DP = 170
        const val HANDLE_DP = 28

        const val SRC_PINS = "pins-source"
        const val LAYER_PINS = "pins-layer"
        const val SRC_USER = "user-source"
        const val LAYER_USER_ACCURACY = "user-accuracy-layer"
        const val LAYER_USER_HALO = "user-halo-layer"
        const val USER_BLUE = "#1A73E8"
        const val LAYER_USER_DOT = "user-dot-layer"

        val DARK_STYLES = listOf(
            "https://tiles.openfreemap.org/styles/fiord",
            "https://tiles.openfreemap.org/styles/dark",
            "https://tiles.openfreemap.org/styles/liberty"
        )


        val AREA_BOUNDS: LatLngBounds = LatLngBounds.from(14.100, 120.660, 14.045, 120.605)
    }
}