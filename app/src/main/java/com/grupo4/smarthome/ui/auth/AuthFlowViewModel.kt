package com.grupo4.smarthome.ui.auth

import androidx.lifecycle.ViewModel

class AuthFlowViewModel : ViewModel() {
    var pendingPassword: String = ""
        private set

    fun setPendingPassword(password: String) {
        pendingPassword = password
    }

    fun clear() {
        pendingPassword = ""
    }
}
