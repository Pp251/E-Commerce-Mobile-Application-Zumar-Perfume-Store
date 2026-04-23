package com.varuntulsiyani.project.model

import com.google.firebase.firestore.PropertyName

data class Product(
    var id: String = "",
    var name: String = "",
    @get:PropertyName("image") @set:PropertyName("image")
    var images: List<String> = emptyList(), // First image is thumbnail
    var price: Double = 0.00,
    var description: String = "",
    @get:PropertyName("size_ml") @set:PropertyName("size_ml")
    var size: Int = 0,
    var gender: String = "",
    var stock: Int = 0,
    @get:PropertyName("top_notes") @set:PropertyName("top_notes")
    var topNotes: List<String> = emptyList(),
    @get:PropertyName("heart_notes") @set:PropertyName("heart_notes")
    var heartNotes: List<String> = emptyList(),
    @get:PropertyName("base_notes") @set:PropertyName("base_notes")
    var baseNotes: List<String> = emptyList(),
    @get:PropertyName("created_at") @set:PropertyName("created_at")
    var createdAt: String = "",
)
