package com.varuntulsiyani.project.model

data class User(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    var selectedCountry: String = "UAE",
    var selectedLanguage: String = "English",
    var isDarkMode: Boolean = false,
)
