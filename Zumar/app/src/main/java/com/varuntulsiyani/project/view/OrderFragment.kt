package com.varuntulsiyani.project.view

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.varuntulsiyani.project.adapter.OrderItemAdapter
import com.varuntulsiyani.project.databinding.FragmentOrderBinding
import com.varuntulsiyani.project.viewmodel.OrderViewModel
import com.varuntulsiyani.project.viewmodel.SharedViewModel
import java.util.Locale

class OrderFragment : Fragment() {
    private var _binding: FragmentOrderBinding? = null
    private val binding get() = checkNotNull(_binding)

    private val orderViewModel: OrderViewModel by activityViewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var adapter: OrderItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecycler()
        observeOrder()
        
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecycler() {
        adapter = OrderItemAdapter()
        binding.rvItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvItems.adapter = adapter
    }

    private fun observeOrder() {
        orderViewModel.order.observe(viewLifecycleOwner) { order ->
            val factor = sharedViewModel.getCurrencyFactor()
            val symbol = sharedViewModel.getCurrencySymbol()
            
            adapter.setCurrency(symbol, factor)
            
            binding.apply {
                tvOrderId.text = "Order #${order.id}"

                tvSubtotal.text = String.format(Locale.getDefault(), "Subtotal: %s %.2f", symbol, order.subtotal * factor)
                tvDeliveryFee.text = String.format(Locale.getDefault(), "Delivery Fee: %s %.2f", symbol, order.deliveryFee * factor)
                tvDiscount.text = String.format(Locale.getDefault(), "Discount: %s %.2f", symbol, order.discount * factor)
                tvTotal.text = String.format(Locale.getDefault(), "Total: %s %.2f", symbol, order.calculateTotal() * factor)
                tvOrderStatus.text = "Status: ${order.status}"

                tvOrderStatus.setTextColor(
                    when (order.status) {
                        "Shipped" -> Color.GREEN
                        "Cancelled" -> Color.RED
                        "Processing" -> Color.YELLOW
                        "Pending" -> Color.BLUE
                        else -> Color.GRAY
                    }
                )

                adapter.submitList(order.items)

//            btnTrackOrder.setOnClickListener {
//                findNavController().navigate(R.id.)
//            }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
