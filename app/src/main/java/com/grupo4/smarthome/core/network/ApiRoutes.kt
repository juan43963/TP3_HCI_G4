package com.grupo4.smarthome.core.network

object ApiRoutes {
    const val UsersRegister = "users/register"
    const val UsersLogin = "users/login"
    const val UsersProfile = "users/profile"
    const val UsersVerifyAccount = "users/verify-account"
    const val UsersSendVerification = "users/send-verification"
    const val UsersForgotPassword = "users/forgot-password"
    const val UsersResetPassword = "users/reset-password"
    const val UsersChangePassword = "users/change-password"
    const val UsersLogout = "users/logout"

    const val Homes = "homes"

    fun homeById(homeId: String): String = "$Homes/$homeId"

    fun homeShare(homeId: String): String = "$Homes/$homeId/share"

    fun resolve(path: String): String =
        "${ApiConstants.BaseUrl.trimEnd('/')}/${path.trimStart('/')}"
}
