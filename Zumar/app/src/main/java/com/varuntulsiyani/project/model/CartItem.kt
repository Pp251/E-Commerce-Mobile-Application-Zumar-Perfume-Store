package com.varuntulsiyani.project.model

data class CartItem(
    val id: Int = 0,
    val image: String? = "",
    val name: String = "",
    val price: Double = 0.00,
    var quantity: Int = 0,
)
