package com.varuntulsiyani.project.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.varuntulsiyani.project.databinding.ItemOrderBinding
import com.varuntulsiyani.project.model.OrderItem
import java.util.Locale

class OrderItemAdapter : ListAdapter<OrderItem, OrderItemAdapter.OrderViewHolder>(DiffCallback()) {
    
    private var currencySymbol: String = "AED"
    private var currencyFactor: Double = 1.0

    inner class OrderViewHolder(val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val item = getItem(position)

        with(holder.binding) {
            productName.text = item.name
            productPrice.text = String.format(Locale.getDefault(), "%s %.2f", currencySymbol, item.price * item.quantity * currencyFactor)
            tvQuantity.text = item.quantity.toString()

            Glide.with(root.context)
                .load(item.imageUrl)
                .into(imgProduct)
        }
    }

    fun setCurrency(symbol: String, factor: Double) {
        currencySymbol = symbol
        currencyFactor = factor
        notifyDataSetChanged()
    }

    class DiffCallback : DiffUtil.ItemCallback<OrderItem>() {
        override fun areItemsTheSame(oldItem: OrderItem, newItem: OrderItem): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: OrderItem, newItem: OrderItem): Boolean {
            return oldItem == newItem
        }
    }
}
