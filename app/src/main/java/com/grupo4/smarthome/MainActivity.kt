package com.grupo4.smarthome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.get
import com.grupo4.smarthome.ui.auth.change.ChangePasswordScreen
import com.grupo4.smarthome.ui.auth.change.ChangePasswordScreenTablet
import com.grupo4.smarthome.ui.auth.login.LoginScreen
import com.grupo4.smarthome.ui.auth.login.LoginScreenTablet
import com.grupo4.smarthome.ui.auth.recover.RecoverPasswordScreen
import com.grupo4.smarthome.ui.auth.recover.RecoverPasswordScreenTablet
import com.grupo4.smarthome.ui.auth.register.RegisterScreen
import com.grupo4.smarthome.ui.auth.register.RegisterScreenTablet
import com.grupo4.smarthome.ui.auth.verify.VerifyAccountScreen
import com.grupo4.smarthome.ui.auth.welcome.WelcomeScreen
import com.grupo4.smarthome.ui.auth.welcome.WelcomeScreenTablet
import com.grupo4.smarthome.ui.components.AppDrawerOverlay
import com.grupo4.smarthome.ui.components.LocalOpenDrawer
import com.grupo4.smarthome.ui.components.QuickActionsOverlay
import com.grupo4.smarthome.ui.navigation.AuthScreen
import com.grupo4.smarthome.ui.navigation.Screen
import com.grupo4.smarthome.ui.screens.dashboard.DashboardCallbacks
import com.grupo4.smarthome.ui.screens.dashboard.DashboardPreviewData
import com.grupo4.smarthome.ui.screens.dashboard.layout.phone.PhoneHomeDrawer
import com.grupo4.smarthome.ui.screens.home.HomeScreen
import com.grupo4.smarthome.ui.screens.selecthome.AddHomeOverlay
import com.grupo4.smarthome.ui.screens.selecthome.SelectHomeScreen
import com.grupo4.smarthome.ui.screens.selecthome.SelectableHome
import com.grupo4.smarthome.ui.screens.profile.ProfileScreen
import com.grupo4.smarthome.ui.screens.rooms.RoomsScreen
import com.grupo4.smarthome.ui.screens.routines.CreateRoutineOverlay
import com.grupo4.smarthome.ui.screens.routines.RoutineDetailScreen
import com.grupo4.smarthome.ui.screens.routines.RoutinesScreen
import com.grupo4.smarthome.ui.theme.SmartHomeTheme
import com.grupo4.smarthome.ui.theme.ThemePreference
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var themePreference by rememberSaveable { mutableStateOf(ThemePreference.SYSTEM) }
            SmartHomeTheme(themePreference = themePreference) {
                val windowSize = calculateWindowSizeClass(this)
                SmartHomeApp(
                    windowSize = windowSize,
                    themePreference = themePreference,
                    onThemePreferenceChange = { themePreference = it },
                )
            }
        }
    }
}

@Composable
fun SmartHomeApp(
    windowSize: WindowSizeClass,
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    onThemePreferenceChange: (ThemePreference) -> Unit = {},
) {
    val isTablet = windowSize.widthSizeClass != WindowWidthSizeClass.Compact
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val currentBaseRoute = currentRoute?.substringBefore('?')
    val isSelectHomeRoute = currentBaseRoute == Screen.SELECT_HOME_ROUTE
    val isRoutineDetailRoute = currentBaseRoute?.startsWith(Screen.ROUTINE_DETAIL_ROUTE) == true
    val isMainRoute = currentBaseRoute != null && currentBaseRoute !in AuthScreen.routes

    var isDrawerVisible by remember(currentBaseRoute) { mutableStateOf(false) }
    var showAddHome by rememberSaveable { mutableStateOf(false) }
    var showQuickActions by rememberSaveable { mutableStateOf(false) }
    var showCreateRoutine by rememberSaveable { mutableStateOf(false) }
    val openQuickActions: () -> Unit = { showQuickActions = true }
    val openCreateRoutine: () -> Unit = {
        showQuickActions = false
        showCreateRoutine = true
    }
    val openDrawer: () -> Unit = {
        if (isMainRoute) {
            isDrawerVisible = true
        }
    }
    val closeDrawer: () -> Unit = { isDrawerVisible = false }

    val showBottomBar = currentBaseRoute != null &&
        currentBaseRoute !in AuthScreen.routes &&
        !isSelectHomeRoute &&
        !isRoutineDetailRoute &&
        !isDrawerVisible &&
        !isTablet

    CompositionLocalProvider(LocalOpenDrawer provides openDrawer) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    if (showBottomBar) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary,
                        ) {
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
                    navController.navigate(Screen.SELECT_HOME_ROUTE) {
                        popUpTo(AuthScreen.Welcome.route) { inclusive = true }
                    }
                    Unit
                }
                if (isTablet) {
                    LoginScreenTablet(
                        onNavigateToRegister = onNavigateToRegister,
                        onNavigateToForgotPassword = onNavigateToForgotPassword,
                        onLoginSuccess = onLoginSuccess,
                    )
                } else {
                    LoginScreen(
                        onNavigateToRegister = onNavigateToRegister,
                        onNavigateToForgotPassword = onNavigateToForgotPassword,
                        onLoginSuccess = onLoginSuccess,
                    )
                }
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
                        navController.navigate(Screen.SELECT_HOME_ROUTE) {
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
                val onLinkSent: (String) -> Unit = { email ->
                    navController.navigate(
                        "${AuthScreen.ChangePassword.route}?email=$email"
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
                    )
                } else {
                    RecoverPasswordScreen(
                        onLinkSent = onLinkSent,
                        onBackToLogin = onBackToLogin,
                    )
                }
            }
            composable("${AuthScreen.ChangePassword.route}?email={email}") { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email").orEmpty()
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
                    )
                } else {
                    ChangePasswordScreen(
                        email = email,
                        onPasswordChanged = onPasswordChanged,
                        onBackToLogin = onBackToLogin,
                    )
                }
            }

            composable(Screen.SELECT_HOME_ROUTE) {
                SelectHomeScreen(
                    windowSize = windowSize,
                    userName = "Juan",
                    homes = listOf(
                        SelectableHome("apartment", "Departamento", roomsCount = 8, devicesCount = 22),
                        SelectableHome("office", "Oficina", roomsCount = 0, devicesCount = 0),
                    ),
                    onHomeSelected = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.SELECT_HOME_ROUTE) { inclusive = true }
                        }
                    },
                    onAddHome = { showAddHome = true },
                    onProfileClick = {
                        navController.navigate(Screen.Profile.route) {
                            launchSingleTop = true
                        }
                    },
                )
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
                        }
                    },
                    onQuickActionsOpen = openQuickActions,
                    onCreateRoutine = openCreateRoutine,
                    showQuickAddFab = !isTablet && !showQuickActions && !showCreateRoutine,
                )
            }
            composable(Screen.Rooms.route) {
                RoomsScreen(
                    onProfileClick = {
                        navController.navigate(Screen.Profile.route) {
                            launchSingleTop = true
                        }
                    },
                    onAddRoom = openQuickActions,
                    showQuickAddFab = !isTablet && !showQuickActions && !showCreateRoutine,
                )
            }
            composable(Screen.Routines.route) {
                RoutinesScreen(
                    onProfileClick = {
                        navController.navigate(Screen.Profile.route) {
                            launchSingleTop = true
                        }
                    },
                    onAddRoutine = if (isTablet) openCreateRoutine else openQuickActions,
                    showQuickAddFab = !isTablet && !showQuickActions && !showCreateRoutine,
                    isTablet = isTablet,
                    onNavigateHome = {
                        navController.navigate(Screen.Home.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateRooms = {
                        navController.navigate(Screen.Rooms.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onRoutineClick = { routineId ->
                        if (!isTablet) {
                            navController.navigate("${Screen.ROUTINE_DETAIL_ROUTE}/$routineId")
                        }
                    },
                )
            }
            composable("${Screen.ROUTINE_DETAIL_ROUTE}/{routineId}") {
                RoutineDetailScreen(
                    onBack = { navController.popBackStack() },
                    onDelete = { navController.popBackStack() },
                )
            }
            composable(Screen.Profile.route) {
                val origin = navController.previousBackStackEntry
                    ?.destination?.route?.substringBefore('?')
                val enteredFromHome = origin != null && origin != Screen.SELECT_HOME_ROUTE
                ProfileScreen(
                    windowSize = windowSize,
                    themePreference = themePreference,
                    onThemePreferenceChange = onThemePreferenceChange,
                    enteredFromHome = enteredFromHome,
                    onNavigateHome = {
                        navController.navigate(Screen.Home.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
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
                )
            }
        }
    }

            if (isMainRoute) {
                AppDrawerOverlay(
                    visible = isDrawerVisible,
                    onClose = closeDrawer,
                ) {
                    PhoneHomeDrawer(
                        state = DashboardPreviewData.populated,
                        callbacks = DashboardCallbacks(
                            onAddHomeClick = {
                                closeDrawer()
                                showAddHome = true
                            },
                        ),
                        onClose = closeDrawer,
                    )
                }
            }

            AddHomeOverlay(
                visible = showAddHome,
                isTablet = isTablet,
                onDismiss = { showAddHome = false },
            )

            QuickActionsOverlay(
                visible = showQuickActions,
                onDismiss = { showQuickActions = false },
                onAddRoutine = openCreateRoutine,
                // Lift the overlay above the bottom NavigationBar (M3 default height
                // 80.dp) so its close FAB stays where the "+" FAB was.
                bottomBarPadding = if (showBottomBar) 80.dp else 0.dp,
            )

            CreateRoutineOverlay(
                visible = showCreateRoutine,
                isTablet = isTablet,
                onDismiss = { showCreateRoutine = false },
            )
        }
    }
}
