package com.grupo4.smarthome.ui.auth.verify

import androidx.annotation.StringRes

data class VerifyAccountUiState(
    val code: String = "",
    val isLoading: Boolean = false,
    val verified: Boolean = false,
    val resendCooldownSeconds: Int = 0,
    val hasSent: Boolean = false,
    @StringRes val errorMessage: Int? = null,
) {
    val canSubmit: Boolean
        get() = code.length == VERIFY_CODE_LENGTH && !isLoading

    val canResend: Boolean
        get() = resendCooldownSeconds == 0
}

const val VERIFY_CODE_LENGTH = 16
const val RESEND_COOLDOWN_SECONDS = 15
