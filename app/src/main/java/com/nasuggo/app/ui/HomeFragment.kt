package com.nasuggo.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.nasuggo.app.R
import com.nasuggo.app.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listAdapter = BusinessAdapter(dark = false) { Actions.openDetail(requireContext(), it.business.id) }
        val nearestAdapter = NearestAdapter(
            onOpen = { Actions.openDetail(requireContext(), it.business.id) },
            onMap = {
                viewModel.requestMapFocus(it.business.id)
                (activity as? MainActivity)?.showMap()
            }
        )

        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = ConcatAdapter(nearestAdapter, listAdapter)

        setupCategoryChips(
            binding.chipGroup, layoutInflater, R.layout.item_chip, viewModel,
            getString(R.string.category_all)
        )
        setupBarangayChips(
            binding.barangayChipGroup, layoutInflater, R.layout.item_chip, viewModel,
            getString(R.string.barangay_all)
        )
        setupSortChips(
            binding.sortChipGroup, layoutInflater, R.layout.item_chip, viewModel
        ) { getString(R.string.sort_prefix, it) }
        bindSearchField(binding.searchInput, binding.searchClear, viewModel)

        viewModel.items.observe(viewLifecycleOwner) { list ->
            listAdapter.submitList(list)
            binding.emptyText.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            binding.subtitle.text = getString(
                R.string.header_subtitle,
                resources.getQuantityString(R.plurals.place_count, list.size, list.size)
            )

            if (viewModel.hasRealLocation) {
                binding.locationNotice.visibility = View.GONE
            } else {
                binding.locationNotice.setText(
                    if (viewModel.outsideArea) R.string.location_outside else R.string.location_denied
                )
                binding.locationNotice.visibility = View.VISIBLE
            }
        }

        viewModel.nearest.observe(viewLifecycleOwner) { nearestAdapter.submit(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recycler.adapter = null
        _binding = null
    }
}
