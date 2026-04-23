package com.varuntulsiyani.project.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.varuntulsiyani.project.R
import com.varuntulsiyani.project.adapter.CategoryAdapter
import com.varuntulsiyani.project.adapter.ProductAdapter
import com.varuntulsiyani.project.databinding.FragmentHomeBinding
import com.varuntulsiyani.project.model.Category
import com.varuntulsiyani.project.viewmodel.ProductViewModel
import com.varuntulsiyani.project.viewmodel.SharedViewModel

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = checkNotNull(_binding)
    
    private val productViewModel: ProductViewModel by activityViewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var productAdapter: ProductAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategories()
        setupFeaturedProducts()
        observeData()
        
        productViewModel.fetchProducts()
    }

    private fun setupCategories() {
        val categories = listOf(
            Category("1", "Men", R.drawable.apparel_24),
            Category("2", "Women", R.drawable.apparel_24),
            Category("3", "Unisex", R.drawable.apparel_24),
            Category("4", "All", R.drawable.add_notes_24)
        )

        val adapter = CategoryAdapter(categories) { category ->
            productViewModel.selectedCategory = category.name
            findNavController().navigate(R.id.navigation_product_list)
        }
        binding.rvCategories.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvCategories.adapter = adapter
    }

    private fun setupFeaturedProducts() {
        productAdapter = ProductAdapter { product ->
            val bundle = Bundle().apply {
                putString("productId", product.id)
            }
            findNavController().navigate(R.id.navigation_product_detail, bundle)
        }
        binding.rvFeaturedProducts.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvFeaturedProducts.adapter = productAdapter
    }

    private fun observeData() {
        productViewModel.productList.observe(viewLifecycleOwner) { products ->
            updateUI(products)
        }
        
        sharedViewModel.selectedCountry.observe(viewLifecycleOwner) {
            updateUI(productViewModel.productList.value ?: emptyList())
        }
    }

    private fun updateUI(products: List<com.varuntulsiyani.project.model.Product>) {
        val symbol = sharedViewModel.getCurrencySymbol()
        val factor = sharedViewModel.getCurrencyFactor()
        
        productAdapter.setCurrency(symbol, factor)
        productAdapter.submitList(products.take(4))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
