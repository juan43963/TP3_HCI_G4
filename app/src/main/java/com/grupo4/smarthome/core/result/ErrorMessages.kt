package com.grupo4.smarthome.core.result

import androidx.annotation.StringRes
import com.grupo4.smarthome.R


@StringRes
fun ErrorKind.toMessageRes(): Int =
    when (this) {
        ErrorKind.Unauthorized -> R.string.error_session_expired
        ErrorKind.Network -> R.string.error_network
        ErrorKind.Validation -> R.string.error_validation
        ErrorKind.NotFound -> R.string.error_not_found
        ErrorKind.Server -> R.string.error_server_generic
        ErrorKind.Conflict, ErrorKind.Forbidden, ErrorKind.Unknown -> R.string.error_unknown
    }
