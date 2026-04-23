package com.varuntulsiyani.project.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.varuntulsiyani.project.databinding.ItemOrderListBinding
import com.varuntulsiyani.project.model.Order
import java.util.Locale

class OrderListAdapter(
    private val onOrderClick: (Order) -> Unit
) : ListAdapter<Order, OrderListAdapter.OrderViewHolder>(DiffCallback()) {

    private var currencySymbol: String = "AED"
    private var currencyFactor: Double = 1.0

    inner class OrderViewHolder(val binding: ItemOrderListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding =
            ItemOrderListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position)
        with(holder.binding) {
            tvOrderId.text = "Order #${order.id}"
            tvStatus.text = order.status
            tvItemCount.text = "${order.items.size} Items"
            tvTotalAmount.text = String.format(Locale.getDefault(), "%s %.2f", currencySymbol, order.calculateTotal() * currencyFactor)
            tvDate.text = order.orderDate

            root.setOnClickListener { onOrderClick(order) }
        }
    }

    fun setCurrency(symbol: String, factor: Double) {
        this.currencySymbol = symbol
        this.currencyFactor = factor
        notifyDataSetChanged()
    }

    class DiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }
}
