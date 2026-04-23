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
import com.varuntulsiyani.project.adapter.OrderListAdapter
import com.varuntulsiyani.project.databinding.FragmentOrderListBinding
import com.varuntulsiyani.project.viewmodel.OrderViewModel
import com.varuntulsiyani.project.viewmodel.SharedViewModel

class OrderListFragment : Fragment() {
    private var _binding: FragmentOrderListBinding? = null
    private val binding get() = checkNotNull(_binding)
    
    private val orderViewModel: OrderViewModel by activityViewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var adapter: OrderListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = "My Orders"
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        setupRecyclerView()
        observeData()
        
        orderViewModel.fetchOrders()
    }

    private fun setupRecyclerView() {
        adapter = OrderListAdapter { selectedOrder ->
            orderViewModel.setOrder(selectedOrder)
            findNavController().navigate(R.id.action_order_list_to_details)
        }
        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrders.adapter = adapter
    }

    private fun observeData() {
        orderViewModel.orderList.observe(viewLifecycleOwner) { orders ->
            val symbol = sharedViewModel.getCurrencySymbol()
            val factor = sharedViewModel.getCurrencyFactor()
            
            adapter.setCurrency(symbol, factor)
            adapter.submitList(orders)
            
            if (orders.isNullOrEmpty()) {
                binding.tvEmptyOrders.visibility = View.VISIBLE
                binding.rvOrders.visibility = View.GONE
            } else {
                binding.tvEmptyOrders.visibility = View.GONE
                binding.rvOrders.visibility = View.VISIBLE
            }
        }
        
        sharedViewModel.selectedCountry.observe(viewLifecycleOwner) {
            val symbol = sharedViewModel.getCurrencySymbol()
            val factor = sharedViewModel.getCurrencyFactor()
            adapter.setCurrency(symbol, factor)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
