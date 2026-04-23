package com.varuntulsiyani.project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varuntulsiyani.project.model.CartItem
import com.varuntulsiyani.project.model.Product
import com.varuntulsiyani.project.repository.CartRepository
import kotlinx.coroutines.launch

class CartViewModel : ViewModel() {
    private val repository = CartRepository()
    
    private val _cartItems = MutableLiveData<List<CartItem>>(emptyList())
    val cartItems: LiveData<List<CartItem>> = _cartItems

    private val _discountPercentage = MutableLiveData(0.0)
    val discountPercentage: LiveData<Double> = _discountPercentage

    init {
        loadCart()
    }

    fun applyDiscount(percentage: Double) {
        _discountPercentage.value = percentage
    }

    private fun loadCart() {
        viewModelScope.launch {
            _cartItems.value = repository.getCartItems()
        }
    }

    fun increaseQuantity(item: CartItem) {
        val updatedItem = item.copy(quantity = item.quantity + 1)
        updateItemInList(updatedItem)
        viewModelScope.launch {
            repository.saveCartItem(updatedItem)
        }
    }

    fun decreaseQuantity(item: CartItem) {
        if (item.quantity > 1) {
            val updatedItem = item.copy(quantity = item.quantity - 1)
            updateItemInList(updatedItem)
            viewModelScope.launch {
                repository.saveCartItem(updatedItem)
            }
        }
    }

    private fun updateItemInList(updatedItem: CartItem) {
        val currentList = _cartItems.value?.toMutableList() ?: mutableListOf()
        val index = currentList.indexOfFirst { it.id == updatedItem.id }
        if (index != -1) {
            currentList[index] = updatedItem
            _cartItems.value = currentList
        }
    }

    fun addToCart(product: Product, quantity: Int) {
        viewModelScope.launch {
            val currentList = _cartItems.value?.toMutableList() ?: mutableListOf()
            val existingItemIndex = currentList.indexOfFirst { it.name == product.name }

            val itemToSave: CartItem
            if (existingItemIndex != -1) {
                val existingItem = currentList[existingItemIndex]
                itemToSave = existingItem.copy(quantity = existingItem.quantity + quantity)
                currentList[existingItemIndex] = itemToSave
            } else {
                itemToSave = CartItem(
                    id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                    image = product.images.firstOrNull(),
                    name = product.name,
                    price = product.price,
                    quantity = quantity
                )
                currentList.add(itemToSave)
            }
            _cartItems.value = currentList
            repository.saveCartItem(itemToSave)
        }
    }

    fun removeItem(item: CartItem) {
        val updated = _cartItems.value?.filter { it.id != item.id } ?: emptyList()
        _cartItems.value = updated
        viewModelScope.launch {
            repository.deleteCartItem(item.id)
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    fun getTotal(): Double {
        return _cartItems.value?.sumOf { it.price * it.quantity } ?: 0.0
    }
}
