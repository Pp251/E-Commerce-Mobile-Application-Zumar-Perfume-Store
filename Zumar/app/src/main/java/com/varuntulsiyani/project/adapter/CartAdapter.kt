package com.varuntulsiyani.project.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.varuntulsiyani.project.databinding.ItemCartBinding
import com.varuntulsiyani.project.model.CartItem
import java.util.Locale

class CartAdapter(
    private val onIncrease: (CartItem) -> Unit,
    private val onDecrease: (CartItem) -> Unit,
    private val onRemove: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(DiffCallback()) {

    private var currencySymbol: String = "AED"
    private var currencyFactor: Double = 1.0

    inner class CartViewHolder(val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = getItem(position)

        with(holder.binding) {
            productName.text = item.name
            productPrice.text = String.format(Locale.getDefault(), "%s %.2f", currencySymbol, item.price * item.quantity * currencyFactor)
            tvQuantity.text = item.quantity.toString()

            Glide.with(root.context)
                .load(item.image)
                .into(imgProduct)

            btnPlus.setOnClickListener { onIncrease(item) }
            btnMinus.setOnClickListener { onDecrease(item) }
            // Ensure btnRemove exists or similar if it's in the layout
            root.setOnLongClickListener {
                onRemove(item)
                true
            }
        }
    }

    fun setCurrency(symbol: String, factor: Double) {
        this.currencySymbol = symbol
        this.currencyFactor = factor
        notifyDataSetChanged()
    }

    class DiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem
        }
    }
}
