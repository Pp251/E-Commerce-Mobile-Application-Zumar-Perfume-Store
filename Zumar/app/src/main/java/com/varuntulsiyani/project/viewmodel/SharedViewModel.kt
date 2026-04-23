package com.varuntulsiyani.project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _selectedCountry = MutableLiveData("UAE")
    val selectedCountry: LiveData<String> = _selectedCountry

    fun setCountry(country: String) {
        _selectedCountry.value = country
    }

    fun getCurrencySymbol(): String {
        return when (_selectedCountry.value) {
            "USA" -> "$"
            "UK" -> "£"
            "India" -> "₹"
            else -> "AED"
        }
    }

    fun getCurrencyFactor(): Double {
        return when (_selectedCountry.value) {
            "USA" -> 0.27
            "UK" -> 0.21
            "India" -> 22.5
            else -> 1.0
        }
    }
}
