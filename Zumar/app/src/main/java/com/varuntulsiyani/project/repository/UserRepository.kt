package com.varuntulsiyani.project.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.varuntulsiyani.project.model.Address
import com.varuntulsiyani.project.model.User
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersRef = db.collection("users")

    suspend fun addUser(userId: String, user: User) {
        usersRef.document(userId).set(user).await()
    }

    suspend fun getUser(userId: String): User? {
        val snapshot = usersRef.document(userId).get().await()
        return snapshot.toObject(User::class.java)
    }

    suspend fun updateUserSettings(userId: String, updates: Map<String, Any>) {
        usersRef.document(userId).update(updates).await()
    }

    // Address Management
    suspend fun saveAddress(userId: String, address: Address) {
        val ref = usersRef.document(userId).collection("addresses")
        val id = address.id.ifEmpty { ref.document().id }
        
        val addressToSave = address.copy(id = id)

        // If address is default -> remove default from others
        if (addressToSave.isDefault) {
            val existing = ref.get().await()
            for (doc in existing.documents) {
                val addr = doc.toObject(Address::class.java)
                if (addr?.isDefault == true && doc.id != id) {
                    ref.document(doc.id).update("isDefault", false).await()
                }
            }
        }
        
        ref.document(id).set(addressToSave).await()
    }

    suspend fun getAddresses(userId: String): List<Address> {
        val snapshot = usersRef.document(userId).collection("addresses").get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Address::class.java)?.apply { id = doc.id }
        }
    }

    suspend fun deleteAddress(userId: String, addressId: String) {
        usersRef.document(userId).collection("addresses").document(addressId).delete().await()
    }
}
