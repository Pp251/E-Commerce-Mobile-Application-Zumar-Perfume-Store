package com.varuntulsiyani.project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.varuntulsiyani.project.model.Address
import com.varuntulsiyani.project.model.User
import com.varuntulsiyani.project.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val repository = UserRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val _addresses = MutableLiveData<List<Address>>()
    val addresses: LiveData<List<Address>> = _addresses
    
    private val _authState = MutableLiveData<Boolean>()
    val authState: LiveData<Boolean> = _authState

    fun login(email: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = true
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun signup(email: String, password: String, user: User, onComplete: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    addUser(userId, user) { success ->
                        if (success) {
                            _authState.value = true
                            onComplete(true, null)
                        } else {
                            onComplete(false, "Failed to save user data")
                        }
                    }
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun logout() {
        auth.signOut()
        _authState.value = false
        _user.value = User()
        _addresses.value = emptyList()
    }

    fun addUser(userId: String, user: User, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repository.addUser(userId, user)
                _user.value = user
                onComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    fun fetchUser(userId: String) {
        viewModelScope.launch {
            try {
                val userData = repository.getUser(userId)
                userData?.let { _user.value = it }
            } catch (e: Exception) {
                e.printStackTrace()
                _user.value = User()
            }
        }
    }

    fun updateSetting(field: String, value: Any) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                repository.updateUserSettings(userId, mapOf(field to value))
                // Update local user object
                _user.value?.let { current ->
                    val updated = when(field) {
                        "selectedCountry" -> current.copy(selectedCountry = value as String)
                        "selectedLanguage" -> current.copy(selectedLanguage = value as String)
                        "isDarkMode" -> current.copy(isDarkMode = value as Boolean)
                        else -> current
                    }
                    _user.value = updated
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchAddresses(userId: String) {
        viewModelScope.launch {
            try {
                val addressList = repository.getAddresses(userId)
                _addresses.value = addressList
            } catch (e: Exception) {
                _addresses.value = emptyList()
            }
        }
    }

    fun saveAddress(userId: String, address: Address) {
        viewModelScope.launch {
            try {
                repository.saveAddress(userId, address)
                fetchAddresses(userId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteAddress(userId: String, addressId: String) {
        viewModelScope.launch {
            try {
                repository.deleteAddress(userId, addressId)
                fetchAddresses(userId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
