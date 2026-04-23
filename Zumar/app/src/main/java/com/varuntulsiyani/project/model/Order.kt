package com.varuntulsiyani.project.model

data class Order(
    var id: Int = 0,
    var items: List<OrderItem> = emptyList(),
    var subtotal: Double = 0.00,
    var deliveryFee: Double = 0.00,
    var discount: Double = 0.00,
    var status: String = "",
    var orderDate: String = "",
) {
    fun calculateTotal(): Double {
        return subtotal + deliveryFee - discount
    }
}
