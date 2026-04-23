package com.varuntulsiyani.project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varuntulsiyani.project.model.Product
import com.varuntulsiyani.project.repository.ProductRepository
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {
    private val repository = ProductRepository()

    private val _product = MutableLiveData<Product>()
    val product: LiveData<Product> = _product

    private val _productList = MutableLiveData<List<Product>>()
    val productList: LiveData<List<Product>> = _productList
    
    val filteredProductList = MediatorLiveData<List<Product>>().apply {
        addSource(productList) { applyFilter() }
    }

    var selectedCategory: String = "All"
        set(value) {
            field = value
            applyFilter()
        }
    var selectedNotes = linkedSetOf<String>()
        set(value) {
            field = value
            applyFilter()
        }
    var selectedSortBy: String = SORT_DEFAULT
        set(value) {
            field = value
            applyFilter()
        }
    var searchQuery: String = ""
        set(value) {
            field = value
            applyFilter()
        }

    companion object {
        const val SORT_DEFAULT = "default"
        const val SORT_PRICE_ASC = "price_asc"
        const val SORT_PRICE_DESC = "price_desc"
        const val SORT_SIZE_ASC = "size_asc"
        const val SORT_SIZE_DESC = "size_desc"
    }

    fun fetchProduct(productId: String) {
        viewModelScope.launch {
            try {
                val productData = repository.getProduct(productId)
                productData?.let { _product.value = it }
            } catch (e: Exception) {
                e.printStackTrace()
                _product.value = Product()
            }
        }
    }

    fun fetchProducts() {
        viewModelScope.launch {
            try {
                val productData = repository.getProducts()
                productData?.let { _productList.value = it }
            } catch (e: Exception) {
                e.printStackTrace()
                _productList.value = emptyList()
            }
        }
    }

    fun applyFilter() {
        val products = productList.value ?: return

        var filtered = if (selectedCategory.equals("All", true)) {
            products
        } else {
            products.filter { it.gender.equals(selectedCategory, true) }
        }

        if (searchQuery.isNotEmpty()) {
            filtered = filtered.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }

        if (selectedNotes.isNotEmpty()) {
            filtered = filtered.filter { product ->
                val notes = product.topNotes + product.heartNotes + product.baseNotes
                selectedNotes.any { selected ->
                    notes.any { it.equals(selected, true) }
                }
            }
        }

        val sorted = when (selectedSortBy) {
            SORT_PRICE_ASC -> filtered.sortedBy { it.price }
            SORT_PRICE_DESC -> filtered.sortedByDescending { it.price }
            SORT_SIZE_ASC -> filtered.sortedBy { it.size }
            SORT_SIZE_DESC -> filtered.sortedByDescending { it.size }
            else -> filtered
        }

        filteredProductList.postValue(sorted)
    }
}
