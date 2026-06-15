package com.grupo4.smarthome.ui.navigation

import android.widget.Toast
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.grupo4.smarthome.R
import com.grupo4.smarthome.SmartHomeApplication
import com.grupo4.smarthome.ui.auth.change.AuthChangePasswordScreen
import com.grupo4.smarthome.ui.auth.change.AuthChangePasswordScreenTablet
import com.grupo4.smarthome.ui.auth.change.AuthChangePasswordViewModel
import com.grupo4.smarthome.ui.auth.change.AuthChangePasswordViewModelFactory

fun NavGraphBuilder.accountGraph(
    navController: NavHostController,
    isTablet: Boolean,
) {
    composable(Screen.CHANGE_PASSWORD_ROUTE) {
        val application = LocalContext.current.applicationContext as SmartHomeApplication
        val context = LocalContext.current
        val authChangeFactory = remember(application) {
            AuthChangePasswordViewModelFactory(application.appContainer.authRepository)
        }
        val authChangeViewModel: AuthChangePasswordViewModel =
            viewModel(factory = authChangeFactory)
        val passwordChangedMessage = stringResource(R.string.auth_change_pwd_success)
        val onPasswordChanged: () -> Unit = {
            Toast.makeText(
                context,
                passwordChangedMessage,
                Toast.LENGTH_LONG,
            ).show()
            navController.popBackStack()
        }
        val onBack: () -> Unit = { navController.popBackStack() }
        if (isTablet) {
            AuthChangePasswordScreenTablet(
                onPasswordChanged = onPasswordChanged,
                onBack = onBack,
                viewModel = authChangeViewModel,
            )
        } else {
            AuthChangePasswordScreen(
                onPasswordChanged = onPasswordChanged,
                onBack = onBack,
                viewModel = authChangeViewModel,
            )
        }
    }
}
