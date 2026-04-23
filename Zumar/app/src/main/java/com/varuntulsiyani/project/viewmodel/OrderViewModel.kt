package com.varuntulsiyani.project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varuntulsiyani.project.model.Order
import com.varuntulsiyani.project.repository.OrderRepository
import kotlinx.coroutines.launch

class OrderViewModel : ViewModel() {
    private val repository = OrderRepository()

    private val _orderList = MutableLiveData<List<Order>>()
    val orderList: LiveData<List<Order>> = _orderList

    private val _order = MutableLiveData<Order>()
    val order: LiveData<Order> = _order

    init {
        fetchOrders()
    }

    fun fetchOrders() {
        viewModelScope.launch {
            _orderList.value = repository.getOrders()
        }
    }

    fun placeOrder(order: Order, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.placeOrder(order)
            if (success) {
                fetchOrders()
            }
            onComplete(success)
        }
    }

    fun setOrder(order: Order) {
        _order.value = order
    }
}
