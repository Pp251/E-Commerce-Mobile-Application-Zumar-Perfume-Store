package com.varuntulsiyani.project.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.varuntulsiyani.project.R
import com.varuntulsiyani.project.adapter.ProductAdapter
import com.varuntulsiyani.project.databinding.FragmentProductListBinding
import com.varuntulsiyani.project.viewmodel.ProductViewModel
import com.varuntulsiyani.project.viewmodel.ProductViewModel.Companion.SORT_DEFAULT
import com.varuntulsiyani.project.viewmodel.ProductViewModel.Companion.SORT_PRICE_ASC
import com.varuntulsiyani.project.viewmodel.ProductViewModel.Companion.SORT_PRICE_DESC
import com.varuntulsiyani.project.viewmodel.ProductViewModel.Companion.SORT_SIZE_ASC
import com.varuntulsiyani.project.viewmodel.ProductViewModel.Companion.SORT_SIZE_DESC
import com.varuntulsiyani.project.viewmodel.SharedViewModel

class ProductListFragment : Fragment() {
    private var _binding: FragmentProductListBinding? = null
    private val binding get() = checkNotNull(_binding)

    private val productViewModel: ProductViewModel by activityViewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var adapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchView()
        setupButtons()
        observeData()

        productViewModel.fetchProducts()
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter { product ->
            val bundle = Bundle().apply { putString("productId", product.id) }
            findNavController().navigate(R.id.action_product_list_to_detail, bundle)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                productViewModel.searchQuery = newText ?: ""
                return true
            }
        })
    }

    private fun setupButtons() {
        binding.filterButton.setOnClickListener { showFilterMenu(it) }
        binding.sortButton.setOnClickListener { showSortMenu(it) }
    }

    private fun observeData() {
        productViewModel.filteredProductList.observe(viewLifecycleOwner) { products ->
            updateUI(products)
        }
        
        sharedViewModel.selectedCountry.observe(viewLifecycleOwner) {
            updateUI(productViewModel.filteredProductList.value ?: emptyList())
        }
    }

    private fun updateUI(products: List<com.varuntulsiyani.project.model.Product>) {
        val symbol = sharedViewModel.getCurrencySymbol()
        val factor = sharedViewModel.getCurrencyFactor()
        
        adapter.setCurrency(symbol, factor)
        adapter.submitList(products)
        updateSubtitle()
    }

    private fun showFilterMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menu.add(0, 0, 0, "All")
        popup.menu.add(0, 1, 1, "Male")
        popup.menu.add(0, 2, 2, "Female")
        popup.menu.add(0, 3, 3, "Unisex")

        popup.setOnMenuItemClickListener { item ->
            productViewModel.selectedCategory = item.title.toString()
            true
        }
        popup.show()
    }

    private fun showSortMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menu.add(0, 1, 0, "Default")
        popup.menu.add(0, 2, 1, "Price: Low to High")
        popup.menu.add(0, 3, 2, "Price: High to Low")
        popup.menu.add(0, 4, 3, "Size: Low to High")
        popup.menu.add(0, 5, 4, "Size: High to Low")

        popup.setOnMenuItemClickListener { item ->
            productViewModel.selectedSortBy = when (item.itemId) {
                2 -> SORT_PRICE_ASC
                3 -> SORT_PRICE_DESC
                4 -> SORT_SIZE_ASC
                5 -> SORT_SIZE_DESC
                else -> SORT_DEFAULT
            }
            true
        }
        popup.show()
    }

    private fun updateSubtitle() {
        val parts = mutableListOf<String>()
        if (productViewModel.selectedCategory != "All") parts.add(productViewModel.selectedCategory)
        if (productViewModel.searchQuery.isNotEmpty()) parts.add("Search: ${productViewModel.searchQuery}")

        binding.collectionSubtitle.text =
            if (parts.isEmpty()) "All Products" else "Showing: ${parts.joinToString(" | ")}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
