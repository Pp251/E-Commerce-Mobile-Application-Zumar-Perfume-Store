package com.varuntulsiyani.project.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.varuntulsiyani.project.NotificationHelper
import com.varuntulsiyani.project.R
import com.varuntulsiyani.project.databinding.FragmentPaymentBinding
import com.varuntulsiyani.project.model.Order
import com.varuntulsiyani.project.model.OrderItem
import com.varuntulsiyani.project.viewmodel.CartViewModel
import com.varuntulsiyani.project.viewmodel.OrderViewModel
import com.varuntulsiyani.project.viewmodel.SharedViewModel
import java.time.LocalDate
import java.util.Date
import java.util.Locale

class PaymentFragment : Fragment() {
    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = checkNotNull(_binding)
    
    private val cartViewModel: CartViewModel by activityViewModels()
    private val orderViewModel: OrderViewModel by activityViewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var notificationHelper: NotificationHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        notificationHelper = NotificationHelper(requireContext())

        setupSummary()
        setupPaymentOptions()

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnPayNow.setOnClickListener { processCheckout() }
    }

    private fun setupSummary() {
        val factor = sharedViewModel.getCurrencyFactor()
        val symbol = sharedViewModel.getCurrencySymbol()
        
        val subtotal = cartViewModel.getTotal()
        val discountPercent = cartViewModel.discountPercentage.value ?: 0.0
        val discountAmount = subtotal * discountPercent
        val deliveryFee = if (subtotal > 500) 0.0 else 15.0
        val total = subtotal - discountAmount + deliveryFee

        binding.apply {
            tvSubtotal.text = String.format(Locale.getDefault(), "%s %.2f", symbol, subtotal * factor)
            tvDelivery.text = String.format(Locale.getDefault(), "%s %.2f", symbol, deliveryFee * factor)
            
            if (discountAmount > 0) {
                discountRow.visibility = View.VISIBLE
                tvDiscount.text = String.format(Locale.getDefault(), "-%s %.2f", symbol, discountAmount * factor)
            } else {
                discountRow.visibility = View.GONE
            }
            
            tvTotal.text = String.format(Locale.getDefault(), "%s %.2f", symbol, total * factor)
        }
    }

    private fun setupPaymentOptions() {
        binding.paymentOptions.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbCOD) {
                binding.cardDetailsLayout.visibility = View.GONE
                binding.btnPayNow.text = "Place Order"
            } else {
                binding.cardDetailsLayout.visibility = View.VISIBLE
                binding.btnPayNow.text = "Pay Now"
            }
        }
    }

    private fun processCheckout() {
        val cartItems = cartViewModel.cartItems.value ?: return
        if (cartItems.isEmpty()) {
            Toast.makeText(requireContext(), "Cart is empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (binding.rbCard.isChecked) {
            val cardNumber = binding.etCardNumber.text.toString()
            if (cardNumber.length < 16) {
                Toast.makeText(requireContext(), "Please enter a valid card number", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val orderItems = cartItems.map {
            OrderItem(it.name, it.price, it.quantity, it.image)
        }

        val subtotal = cartViewModel.getTotal()
        val deliveryFee = if (subtotal > 500) 0.0 else 15.0
        val discount = subtotal * (cartViewModel.discountPercentage.value ?: 0.0)

        val newOrder = Order(
            id = (System.currentTimeMillis() / 1000).toInt(),
            items = orderItems,
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            discount = discount,
            status = "Processing",
            orderDate = LocalDate.now().toString()
        )

        orderViewModel.placeOrder(newOrder) { success ->
            if (success) {
                notificationHelper.sendOrderPlacedNotification(newOrder.id.toString())
                cartViewModel.clearCart()
                Toast.makeText(requireContext(), "Order Placed Successfully!", Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.navigation_order_list)
            } else {
                Toast.makeText(requireContext(), "Failed to place order. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
