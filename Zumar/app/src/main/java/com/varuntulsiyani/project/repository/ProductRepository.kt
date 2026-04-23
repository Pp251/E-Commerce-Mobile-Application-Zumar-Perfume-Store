package com.varuntulsiyani.project.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.varuntulsiyani.project.model.Product
import kotlinx.coroutines.tasks.await

class ProductRepository {
    private val db = FirebaseFirestore.getInstance()
    private val productsRef = db.collection("products")

    suspend fun addProduct(productId: String, product: Product) {
        productsRef.document(productId).set(product).await()
    }

    suspend fun getProduct(productId: String): Product? {
        val snapshot = productsRef.document(productId).get().await()
        return snapshot.toObject(Product::class.java)
    }

    suspend fun getProducts(): List<Product>? {
        val snapshot = productsRef.get().await()
        return snapshot.documents.mapNotNull { it.toObject(Product::class.java) }
    }

    fun observeProducts(
        onUpdate: (List<Product>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        productsRef.addSnapshotListener { snapshots, error ->
            if (error != null) {
                onError(error)
                return@addSnapshotListener
            }

            if (snapshots != null) {
                val products = snapshots.documents.mapNotNull {
                    it.toObject(Product::class.java)
                }
                onUpdate(products)
            }
        }
    }
}
