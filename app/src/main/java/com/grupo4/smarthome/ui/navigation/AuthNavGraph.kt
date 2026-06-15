package com.grupo4.smarthome.ui.navigation

import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.grupo4.smarthome.R
import com.grupo4.smarthome.SmartHomeApplication
import com.grupo4.smarthome.ui.auth.AuthFlowViewModel
import com.grupo4.smarthome.ui.auth.change.ChangePasswordScreen
import com.grupo4.smarthome.ui.auth.change.ChangePasswordScreenTablet
import com.grupo4.smarthome.ui.auth.change.ChangePasswordViewModel
import com.grupo4.smarthome.ui.auth.change.ChangePasswordViewModelFactory
import com.grupo4.smarthome.ui.auth.login.LoginScreen
import com.grupo4.smarthome.ui.auth.login.LoginScreenTablet
import com.grupo4.smarthome.ui.auth.login.LoginViewModel
import com.grupo4.smarthome.ui.auth.login.LoginViewModelFactory
import com.grupo4.smarthome.ui.auth.recover.RecoverPasswordScreen
import com.grupo4.smarthome.ui.auth.recover.RecoverPasswordScreenTablet
import com.grupo4.smarthome.ui.auth.recover.RecoverPasswordViewModel
import com.grupo4.smarthome.ui.auth.recover.RecoverPasswordViewModelFactory
import com.grupo4.smarthome.ui.auth.register.RegisterScreen
import com.grupo4.smarthome.ui.auth.register.RegisterScreenTablet
import com.grupo4.smarthome.ui.auth.register.RegisterViewModel
import com.grupo4.smarthome.ui.auth.register.RegisterViewModelFactory
import com.grupo4.smarthome.ui.auth.verify.VerifyAccountScreen
import com.grupo4.smarthome.ui.auth.verify.VerifyAccountViewModel
import com.grupo4.smarthome.ui.auth.verify.VerifyAccountViewModelFactory
import com.grupo4.smarthome.ui.auth.welcome.WelcomeScreen
import com.grupo4.smarthome.ui.auth.welcome.WelcomeScreenTablet

fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    isTablet: Boolean,
    hasSelectedHome: Boolean,
    authFlowViewModel: AuthFlowViewModel,
) {
    composable(AuthScreen.Welcome.route) {
        val onRegister = { navController.navigate(AuthScreen.Register.route) }
        val onLogin = { navController.navigate(AuthScreen.Login.route) }
        if (isTablet) {
            WelcomeScreenTablet(
                onNavigateToRegister = onRegister,
                onNavigateToLogin = onLogin,
            )
        } else {
            WelcomeScreen(
                onNavigateToRegister = onRegister,
                onNavigateToLogin = onLogin,
            )
        }
    }
    composable(AuthScreen.Login.route) {
        val application = LocalContext.current.applicationContext as SmartHomeApplication
        val loginFactory = remember(application) {
            LoginViewModelFactory(application.appContainer.authRepository)
        }
        val loginViewModel: LoginViewModel = viewModel(factory = loginFactory)
        val onNavigateToRegister = {
            navController.navigate(AuthScreen.Register.route) {
                popUpTo(AuthScreen.Login.route) { inclusive = true }
            }
            Unit
        }
        val onNavigateToForgotPassword = {
            navController.navigate(AuthScreen.RecoverPassword.route)
            Unit
        }
        val onLoginSuccess = {
            val target = if (hasSelectedHome) Screen.Home.route else Screen.SELECT_HOME_ROUTE
            navController.navigate(target) {
                popUpTo(AuthScreen.Welcome.route) { inclusive = true }
            }
            Unit
        }
        val loginContext = LocalContext.current
        val accountNotVerifiedMessage = stringResource(R.string.login_account_not_verified)
        val onNeedsVerification: (String, String) -> Unit = { email, password ->
            Toast.makeText(
                loginContext,
                accountNotVerifiedMessage,
                Toast.LENGTH_LONG,
            ).show()
            authFlowViewModel.setPendingPassword(password)
            navController.navigate(
                AuthScreen.VerifyAccount.createRoute(Uri.encode(email))
            )
        }
        if (isTablet) {
            LoginScreenTablet(
                onNavigateToRegister = onNavigateToRegister,
                onNavigateToForgotPassword = onNavigateToForgotPassword,
                onLoginSuccess = onLoginSuccess,
                onNeedsVerification = onNeedsVerification,
                viewModel = loginViewModel,
            )
        } else {
            LoginScreen(
                onNavigateToRegister = onNavigateToRegister,
                onNavigateToForgotPassword = onNavigateToForgotPassword,
                onLoginSuccess = onLoginSuccess,
                onNeedsVerification = onNeedsVerification,
                viewModel = loginViewModel,
            )
        }
    }
    composable(AuthScreen.Register.route) {
        val application = LocalContext.current.applicationContext as SmartHomeApplication
        val registerFactory = remember(application) {
            RegisterViewModelFactory(application.appContainer.authRepository)
        }
        val registerViewModel: RegisterViewModel = viewModel(factory = registerFactory)
        val onNavigateToLogin = {
            navController.navigate(AuthScreen.Login.route) {
                popUpTo(AuthScreen.Register.route) { inclusive = true }
            }
            Unit
        }
        val onRegisterSuccess: (String, String) -> Unit = { email, password ->
            authFlowViewModel.setPendingPassword(password)
            navController.navigate(
                AuthScreen.VerifyAccount.createRoute(Uri.encode(email))
            )
            Unit
        }
        if (isTablet) {
            RegisterScreenTablet(
                onNavigateToLogin = onNavigateToLogin,
                onRegisterSuccess = onRegisterSuccess,
                viewModel = registerViewModel,
            )
        } else {
            RegisterScreen(
                onNavigateToLogin = onNavigateToLogin,
                onRegisterSuccess = onRegisterSuccess,
                viewModel = registerViewModel,
            )
        }
    }
    composable(
        route = AuthScreen.VerifyAccount.routeWithEmail,
        arguments = listOf(
            navArgument(AuthScreen.VerifyAccount.emailArg) {
                type = NavType.StringType
                defaultValue = ""
            }
        ),
    ) { backStackEntry ->
        val email = backStackEntry.arguments
            ?.getString(AuthScreen.VerifyAccount.emailArg)
            .orEmpty()
        val password = authFlowViewModel.pendingPassword
        val application = LocalContext.current.applicationContext as SmartHomeApplication
        val verifyFactory = remember(application) {
            VerifyAccountViewModelFactory(application.appContainer.authRepository)
        }
        val verifyViewModel: VerifyAccountViewModel = viewModel(factory = verifyFactory)
        VerifyAccountScreen(
            email = email,
            password = password,
            onVerifySuccess = {
                authFlowViewModel.clear()
                navController.navigate(Screen.CREATE_FIRST_HOME_ROUTE) {
                    popUpTo(AuthScreen.Welcome.route) { inclusive = true }
                }
            },
            onChangeEmail = { navController.popBackStack() },
            onBackToLogin = {
                authFlowViewModel.clear()
                navController.navigate(AuthScreen.Login.route) {
                    popUpTo(AuthScreen.Welcome.route)
                }
            },
            viewModel = verifyViewModel,
        )
    }
    composable(AuthScreen.RecoverPassword.route) {
        val application = LocalContext.current.applicationContext as SmartHomeApplication
        val recoverFactory = remember(application) {
            RecoverPasswordViewModelFactory(application.appContainer.authRepository)
        }
        val recoverViewModel: RecoverPasswordViewModel = viewModel(factory = recoverFactory)
        val onLinkSent: (String) -> Unit = { email ->
            navController.navigate(
                "${AuthScreen.ChangePassword.route}?email=${Uri.encode(email)}"
            )
        }
        val onBackToLogin: () -> Unit = {
            navController.navigate(AuthScreen.Login.route) {
                popUpTo(AuthScreen.Welcome.route)
            }
        }
        if (isTablet) {
            RecoverPasswordScreenTablet(
                onLinkSent = onLinkSent,
                onBackToLogin = onBackToLogin,
                viewModel = recoverViewModel,
            )
        } else {
            RecoverPasswordScreen(
                onLinkSent = onLinkSent,
                onBackToLogin = onBackToLogin,
                viewModel = recoverViewModel,
            )
        }
    }
    composable("${AuthScreen.ChangePassword.route}?email={email}") { backStackEntry ->
        val email = backStackEntry.arguments?.getString("email").orEmpty()
        val application = LocalContext.current.applicationContext as SmartHomeApplication
        val changeFactory = remember(application) {
            ChangePasswordViewModelFactory(application.appContainer.authRepository)
        }
        val changeViewModel: ChangePasswordViewModel = viewModel(factory = changeFactory)
        val onPasswordChanged: () -> Unit = {
            navController.navigate(AuthScreen.Login.route) {
                popUpTo(AuthScreen.Welcome.route)
            }
        }
        val onBackToLogin: () -> Unit = {
            navController.navigate(AuthScreen.Login.route) {
                popUpTo(AuthScreen.Welcome.route)
            }
        }
        if (isTablet) {
            ChangePasswordScreenTablet(
                email = email,
                onPasswordChanged = onPasswordChanged,
                onBackToLogin = onBackToLogin,
                viewModel = changeViewModel,
            )
        } else {
            ChangePasswordScreen(
                email = email,
                onPasswordChanged = onPasswordChanged,
                onBackToLogin = onBackToLogin,
                viewModel = changeViewModel,
            )
        }
    }
}
