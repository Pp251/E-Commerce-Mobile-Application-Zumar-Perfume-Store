package com.varuntulsiyani.project.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.varuntulsiyani.project.model.CartItem
import kotlinx.coroutines.tasks.await

class CartRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getUserId(): String? = auth.currentUser?.uid

    suspend fun getCartItems(): List<CartItem> {
        val userId = getUserId() ?: return emptyList()
        return try {
            val snapshot = firestore.collection("users").document(userId)
                .collection("cart").get().await()
            snapshot.toObjects(CartItem::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveCartItem(item: CartItem) {
        val userId = getUserId() ?: return
        try {
            firestore.collection("users").document(userId)
                .collection("cart").document(item.id.toString()).set(item).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteCartItem(itemId: Int) {
        val userId = getUserId() ?: return
        try {
            firestore.collection("users").document(userId)
                .collection("cart").document(itemId.toString()).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun clearCart() {
        val userId = getUserId() ?: return
        try {
            val cartRef = firestore.collection("users").document(userId).collection("cart")
            val snapshot = cartRef.get().await()
            for (doc in snapshot.documents) {
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
