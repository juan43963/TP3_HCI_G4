package com.grupo4.smarthome.ui.navigation

sealed class AuthScreen(val route: String) {
    object Welcome : AuthScreen("welcome")
    object Login : AuthScreen("login")
    object Register : AuthScreen("register")
    object VerifyAccount : AuthScreen("verify_account") {
        const val emailArg = "email"
        const val passwordArg = "password"
        const val routeWithEmail = "verify_account?email={$emailArg}&password={$passwordArg}"

        fun createRoute(email: String, password: String): String =
            "verify_account?email=$email&password=$password"
    }
    object RecoverPassword : AuthScreen("recover_password")
    object ChangePassword : AuthScreen("change_password")

    companion object {
        val routes: Set<String> by lazy {
            setOf(
                Welcome.route,
                Login.route,
                Register.route,
                VerifyAccount.route,
                RecoverPassword.route,
                ChangePassword.route,
            )
        }
    }
}
