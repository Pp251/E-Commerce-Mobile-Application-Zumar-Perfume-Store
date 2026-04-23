package com.varuntulsiyani.project.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.varuntulsiyani.project.R
import com.varuntulsiyani.project.adapter.CartAdapter
import com.varuntulsiyani.project.databinding.FragmentCartBinding
import com.varuntulsiyani.project.model.CartItem
import com.varuntulsiyani.project.viewmodel.CartViewModel
import com.varuntulsiyani.project.viewmodel.SharedViewModel
import java.util.Locale

class CartFragment : Fragment() {
    private var _binding: FragmentCartBinding? = null
    private val binding get() = checkNotNull(_binding)

    private val cartViewModel: CartViewModel by activityViewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var adapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecycler()
        setupCouponLogic()
        observeData()

        binding.tvTitle.text = "Cart"
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.btnCheckout.setOnClickListener {
            if (cartViewModel.cartItems.value.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Your cart is empty", Toast.LENGTH_SHORT).show()
            } else {
                findNavController().navigate(R.id.action_cart_to_payment)
            }
        }

        binding.btnStartShopping.setOnClickListener {
            findNavController().navigate(R.id.navigation_home)
        }
    }

    private fun setupRecycler() {
        adapter = CartAdapter(
            onIncrease = { cartViewModel.increaseQuantity(it) },
            onDecrease = { cartViewModel.decreaseQuantity(it) },
            onRemove = { cartViewModel.removeItem(it) }
        )

        binding.rvCartItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCartItems.adapter = adapter
    }

    private fun setupCouponLogic() {
        binding.btnApplyCoupon.setOnClickListener {
            val code = binding.etCouponCode.text.toString().uppercase(Locale.getDefault())
            val validCoupons = mapOf(
                "SAVE10" to 0.10,
                "WELCOME20" to 0.20,
                "PROMO50" to 0.50
            )

            if (validCoupons.containsKey(code)) {
                val discount = validCoupons[code] ?: 0.0
                cartViewModel.applyDiscount(discount)
                Toast.makeText(requireContext(), "Coupon Applied!", Toast.LENGTH_SHORT).show()
            } else {
                cartViewModel.applyDiscount(0.0)
                Toast.makeText(requireContext(), "Invalid Coupon Code", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeData() {
        cartViewModel.cartItems.observe(viewLifecycleOwner) { items ->
            updateUI(items)
        }

        cartViewModel.discountPercentage.observe(viewLifecycleOwner) {
            updateUI(cartViewModel.cartItems.value ?: emptyList())
        }

        sharedViewModel.selectedCountry.observe(viewLifecycleOwner) {
            updateUI(cartViewModel.cartItems.value ?: emptyList())
        }
    }

    private fun updateUI(items: List<CartItem>) {
        if (items.isEmpty()) {
            binding.emptyCartLayout.visibility = View.VISIBLE
            binding.cartContentLayout.visibility = View.GONE
        } else {
            binding.emptyCartLayout.visibility = View.GONE
            binding.cartContentLayout.visibility = View.VISIBLE
            
            val symbol = sharedViewModel.getCurrencySymbol()
            val factor = sharedViewModel.getCurrencyFactor()
            
            adapter.setCurrency(symbol, factor)
            adapter.submitList(items)
            
            updateTotal(items, symbol, factor)
        }
    }

    private fun updateTotal(items: List<CartItem>, symbol: String, factor: Double) {
        val subtotal = items.sumOf { (it.price * it.quantity) } * factor
        val deliveryFee = if (subtotal > (500 * factor)) 0.0 else (15.0 * factor)
        val discountPercentage = cartViewModel.discountPercentage.value ?: 0.0
        val discountAmount = subtotal * discountPercentage
        val total = subtotal + deliveryFee - discountAmount

        binding.tvSubtotal.text = String.format(Locale.getDefault(), "Subtotal: %s %.2f", symbol, subtotal)
        binding.tvDeliveryFee.text = String.format(Locale.getDefault(), "Delivery: %s %.2f", symbol, deliveryFee)
        binding.tvDiscount.text = String.format(Locale.getDefault(), "Discount: -%s %.2f", symbol, discountAmount)
        binding.tvTotal.text = String.format(Locale.getDefault(), "Total: %s %.2f", symbol, total)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
