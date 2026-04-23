package com.varuntulsiyani.project.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.varuntulsiyani.project.R
import com.varuntulsiyani.project.databinding.ItemProductBinding
import com.varuntulsiyani.project.model.Product
import java.util.Locale

class ProductAdapter(
    private val onProductClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(DiffCallback()) {

    private var currencySymbol: String = "AED"
    private var currencyFactor: Double = 1.0

    inner class ProductViewHolder(val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position)

        with(holder.binding) {
            productName.text = product.name
            productPrice.text = String.format(Locale.getDefault(), "%s %.2f", currencySymbol, product.price * currencyFactor)
//            productGender.text =
//                if (product.gender.isBlank()) "Gender: N/A"
//                else "Gender: ${product.gender}"
//            productSize.text =
//                if (product.size > 0) "Size: ${product.size}ml"
//                else "Size: N/A"
//            productStock.text = "Stock: ${product.stock}"
//
//            val notePreview = (product.topNotes + product.heartNotes)
//                .filter { it.isNotBlank() }
//                .distinctBy { it.lowercase() }
//                .take(2)
//                .joinToString(", ")
//
//            productNotePreview.text =
//                if (notePreview.isBlank()) "Notes: N/A"
//                else "Notes: $notePreview"

            Glide.with(root.context)
                .load(product.images.firstOrNull())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .centerCrop()
                .into(productImage)

            root.setOnClickListener {
                onProductClick(product)
            }
        }
    }

    fun setCurrency(symbol: String, factor: Double) {
        if (this.currencySymbol != symbol || this.currencyFactor != factor) {
            this.currencySymbol = symbol
            this.currencyFactor = factor
            notifyDataSetChanged()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}
