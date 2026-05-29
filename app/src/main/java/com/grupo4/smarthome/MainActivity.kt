package com.grupo4.smarthome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.get
import com.grupo4.smarthome.ui.auth.login.LoginScreen
import com.grupo4.smarthome.ui.auth.register.RegisterScreen
import com.grupo4.smarthome.ui.auth.register.RegisterScreenTablet
import com.grupo4.smarthome.ui.auth.verify.VerifyAccountScreen
import com.grupo4.smarthome.ui.auth.welcome.WelcomeScreen
import com.grupo4.smarthome.ui.auth.welcome.WelcomeScreenTablet
import com.grupo4.smarthome.ui.navigation.AuthScreen
import com.grupo4.smarthome.ui.navigation.Screen
import com.grupo4.smarthome.ui.screens.home.HomeScreen
import com.grupo4.smarthome.ui.screens.profile.ProfileScreen
import com.grupo4.smarthome.ui.screens.rooms.RoomsScreen
import com.grupo4.smarthome.ui.screens.routines.RoutinesScreen
import com.grupo4.smarthome.ui.theme.SmartHomeTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartHomeTheme {
                val windowSize = calculateWindowSizeClass(this)
                SmartHomeApp(windowSize = windowSize)
            }
        }
    }
}

@Composable
fun SmartHomeApp(windowSize: WindowSizeClass) {
    val isTablet = windowSize.widthSizeClass != WindowWidthSizeClass.Compact
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val isDashboardRoute = currentRoute == Screen.Home.route
    val showBottomBar = currentRoute != null && currentRoute !in AuthScreen.routes && !isDashboardRoute

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    Screen.items.forEach { screen ->
                        val selected = currentDestination.hierarchy.any { it.route == screen.route }
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph[Screen.Home.route].id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(stringResource(screen.titleRes)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AuthScreen.Welcome.route,
            modifier = Modifier.padding(innerPadding)
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
                LoginScreen(
                    onNavigateToRegister = {
                        navController.navigate(AuthScreen.Register.route) {
                            popUpTo(AuthScreen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(AuthScreen.RecoverPassword.route)
                    },
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(AuthScreen.Welcome.route) { inclusive = true }
                        }
                    },
                )
            }
            composable(AuthScreen.Register.route) {
                val onNavigateToLogin = {
                    navController.navigate(AuthScreen.Login.route) {
                        popUpTo(AuthScreen.Register.route) { inclusive = true }
                    }
                    Unit
                }
                val onRegisterSuccess = {
                    navController.navigate(AuthScreen.VerifyAccount.route)
                    Unit
                }
                if (isTablet) {
                    RegisterScreenTablet(
                        onNavigateToLogin = onNavigateToLogin,
                        onRegisterSuccess = onRegisterSuccess,
                    )
                } else {
                    RegisterScreen(
                        onNavigateToLogin = onNavigateToLogin,
                        onRegisterSuccess = onRegisterSuccess,
                    )
                }
            }
            composable(AuthScreen.VerifyAccount.route) {
                VerifyAccountScreen(
                    email = "juanperez@gmail.com",
                    onVerifySuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(AuthScreen.Welcome.route) { inclusive = true }
                        }
                    },
                    onChangeEmail = { navController.popBackStack() },
                    onBackToLogin = {
                        navController.navigate(AuthScreen.Login.route) {
                            popUpTo(AuthScreen.Welcome.route)
                        }
                    },
                )
            }
            composable(AuthScreen.RecoverPassword.route) {
                AuthPlaceholder(stringResource(R.string.auth_recover_password_placeholder))
            }
            composable(AuthScreen.VerifyAccount.route) {
                AuthPlaceholder(stringResource(R.string.auth_verify_account_placeholder))
            }
            composable(AuthScreen.ChangePassword.route) {
                AuthPlaceholder(stringResource(R.string.auth_change_password_placeholder))
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    windowSize = windowSize,
                    onNavigateRooms = {
                        navController.navigate(Screen.Rooms.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateRoutines = {
                        navController.navigate(Screen.Routines.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateProfile = {
                        navController.navigate(Screen.Profile.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable(Screen.Rooms.route) { RoomsScreen() }
            composable(Screen.Routines.route) { RoutinesScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }
        }
    }
}

@Composable
private fun AuthPlaceholder(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}
