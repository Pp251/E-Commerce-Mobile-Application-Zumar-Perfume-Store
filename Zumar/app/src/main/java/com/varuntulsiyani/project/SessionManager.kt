package com.varuntulsiyani.project

import android.content.Context

class SessionManager(context: Context) {
    private val preferences = context.getSharedPreferences("app_session", Context.MODE_PRIVATE)

    fun setLoggedIn(isLoggedIn: Boolean) {
        preferences.edit().putBoolean("is_logged_in", isLoggedIn).apply()
    }

    fun isLoggedIn(): Boolean {
        return preferences.getBoolean("is_logged_in", false)
    }

    fun logout() {
        preferences.edit().clear().apply()
    }
}
