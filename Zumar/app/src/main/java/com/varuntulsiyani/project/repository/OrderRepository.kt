package com.varuntulsiyani.project.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.varuntulsiyani.project.model.Order
import kotlinx.coroutines.tasks.await

class OrderRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getUserId(): String? = auth.currentUser?.uid

    suspend fun getOrders(): List<Order> {
        val userId = getUserId() ?: return emptyList()
        return try {
            val snapshot = firestore.collection("users").document(userId)
                .collection("orders")
                .orderBy("id", Query.Direction.DESCENDING)
                .get().await()
            snapshot.toObjects(Order::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun placeOrder(order: Order): Boolean {
        val userId = getUserId() ?: return false
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("orders")
                .document(order.id.toString())
                .set(order).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
