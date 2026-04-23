package com.varuntulsiyani.project.model

data class Address(
    var id: String = "",
    var fullName: String = "",
    var phoneNumber: String = "",
    var streetAddress: String = "",
    var city: String = "",
    var zipCode: String = "",
    var isDefault: Boolean = false
)
