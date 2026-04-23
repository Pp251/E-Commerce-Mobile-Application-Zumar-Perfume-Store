package com.varuntulsiyani.project.model

data class OrderItem(
    var name: String = "",
    var price: Double = 0.0,
    var quantity: Int = 0,
    var imageUrl: String? = "",
)
