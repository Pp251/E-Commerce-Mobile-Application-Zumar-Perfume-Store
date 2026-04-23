package com.varuntulsiyani.project.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.varuntulsiyani.project.R
import com.varuntulsiyani.project.adapter.ImageSliderAdapter
import com.varuntulsiyani.project.databinding.FragmentProductDetailsBinding
import com.varuntulsiyani.project.viewmodel.CartViewModel
import com.varuntulsiyani.project.viewmodel.ProductViewModel
import com.varuntulsiyani.project.viewmodel.SharedViewModel
import java.util.Locale

class ProductDetailsFragment : Fragment() {
    private var _binding: FragmentProductDetailsBinding? = null
    private val binding get() = checkNotNull(_binding)
    
    private val cartViewModel: CartViewModel by activityViewModels()
    private val productViewModel: ProductViewModel by activityViewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val productId = arguments?.getString("productId") ?: return

        productViewModel.fetchProduct(productId)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        productViewModel.product.observe(viewLifecycleOwner) { product ->
            if (product == null || product.id.isEmpty()) return@observe
            
            binding.apply {
                productName.text = product.name
                
                val factor = sharedViewModel.getCurrencyFactor()
                val symbol = sharedViewModel.getCurrencySymbol()
                productPrice.text = String.format(Locale.getDefault(), "%s %.2f", symbol, product.price * factor)
                
                tvDescription.text = product.description
                tvSize.text = "Size: ${product.size}ml"
                
                tvTopNotes.text = "Top: ${product.topNotes.joinToString(", ")}"
                tvHeartNotes.text = "Heart: ${product.heartNotes.joinToString(", ")}"
                tvBaseNotes.text = "Base: ${product.baseNotes.joinToString(", ")}"

                // Image Slider
                val imageList = product.images
                if (imageList.isNotEmpty()) {
                    val adapter = ImageSliderAdapter(imageList)
                    viewPagerImages.adapter = adapter
                    TabLayoutMediator(tabLayoutIndicator, viewPagerImages) { _, _ -> }.attach()
                }

                btnPlus.setOnClickListener {
                    val quantity = tvQuantity.text.toString().toInt() + 1
                    tvQuantity.text = quantity.toString()
                }

                btnMinus.setOnClickListener {
                    val quantity = tvQuantity.text.toString().toInt() - 1
                    if (quantity > 0) tvQuantity.text = quantity.toString()
                }

                btnAddToCart.setOnClickListener {
                    val quantity = tvQuantity.text.toString().toInt()
                    cartViewModel.addToCart(product, quantity)
                    findNavController().navigate(R.id.navigation_cart)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
