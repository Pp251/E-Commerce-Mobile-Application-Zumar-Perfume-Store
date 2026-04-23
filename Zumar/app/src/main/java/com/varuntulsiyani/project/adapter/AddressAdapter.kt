package com.varuntulsiyani.project.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.varuntulsiyani.project.databinding.ItemAddressBinding
import com.varuntulsiyani.project.model.Address

class AddressAdapter(
    private val onEdit: (Address) -> Unit,
    private val onDelete: (Address) -> Unit,
    private val onSelect: (Address) -> Unit
) : ListAdapter<Address, AddressAdapter.AddressViewHolder>(DiffCallback()) {

    inner class AddressViewHolder(val binding: ItemAddressBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val binding = ItemAddressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddressViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val address = getItem(position)
        holder.binding.apply {
            tvName.text = address.fullName
            tvFullAddress.text = "${address.streetAddress}, ${address.city}, ${address.zipCode}"
            tvPhone.text = address.phoneNumber
            rbSelected.isChecked = address.isDefault

            root.setOnClickListener { onSelect(address) }
            btnEdit.setOnClickListener { onEdit(address) }
            btnDelete.setOnClickListener { onDelete(address) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Address>() {
        override fun areItemsTheSame(oldItem: Address, newItem: Address): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Address, newItem: Address): Boolean {
            return oldItem == newItem
        }
    }
}
