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
import com.grupo4.smarthome.ui.navigation.AuthScreen
import com.grupo4.smarthome.ui.navigation.Screen
import com.grupo4.smarthome.ui.screens.dashboard.DashboardCallbacks
import com.grupo4.smarthome.ui.screens.dashboard.DashboardPreviewData
import com.grupo4.smarthome.ui.screens.dashboard.layout.phone.PhoneHomeDrawer
import com.grupo4.smarthome.ui.screens.history.HistoryScreen
import com.grupo4.smarthome.ui.screens.home.HomeScreen
import com.grupo4.smarthome.ui.screens.notifications.NotificationPreferences
import com.grupo4.smarthome.ui.screens.notifications.NotificationPreferencesSaver
import com.grupo4.smarthome.ui.screens.notifications.NotificationSettingsScreen
import com.grupo4.smarthome.ui.screens.notifications.NotificationsScreen
import com.grupo4.smarthome.ui.screens.notifications.sampleNotifications
import com.grupo4.smarthome.ui.screens.selecthome.SelectHomeScreen
import com.grupo4.smarthome.ui.screens.selecthome.SelectableHome
import com.grupo4.smarthome.ui.screens.profile.ProfileScreen
import com.grupo4.smarthome.ui.screens.rooms.RoomsScreen
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
    val isMainRoute = currentBaseRoute != null && currentBaseRoute !in AuthScreen.routes

    var isDrawerVisible by remember(currentBaseRoute) { mutableStateOf(false) }
    val openDrawer: () -> Unit = {
        if (isMainRoute) {
            isDrawerVisible = true
        }
    }
    val closeDrawer: () -> Unit = { isDrawerVisible = false }

    // Estado de notificaciones in-app (badge en la campana + centro de notificaciones).
    var notifications by remember { mutableStateOf(sampleNotifications) }
    var notificationPreferences by rememberSaveable(stateSaver = NotificationPreferencesSaver) {
        mutableStateOf(NotificationPreferences())
    }
    val unreadNotificationCount = notifications.count { !it.read }
    val openNotifications: () -> Unit = {
        navController.navigate(Screen.Notifications.route) { launchSingleTop = true }
    }
    val openNotificationSettings: () -> Unit = {
        navController.navigate(Screen.NotificationSettings.route) { launchSingleTop = true }
    }
    val markAllNotificationsRead: () -> Unit = {
        notifications = notifications.map { it.copy(read = true) }
    }
    val markNotificationRead: (String) -> Unit = { id ->
        notifications = notifications.map { if (it.id == id) it.copy(read = true) else it }
    }
    val acceptInvitation: (String) -> Unit = { id ->
        notifications = notifications.map { notification ->
            if (notification.id == id && notification.invitationHome != null) {
                notification.copy(
                    title = "Te uniste al hogar",
                    message = "Ahora formás parte de “${notification.invitationHome}”.",
                    timeLabel = "ahora",
                    invitationHome = null,
                    read = true,
                )
            } else {
                notification
            }
        }
    }
    val rejectInvitation: (String) -> Unit = { id ->
        notifications = notifications.filterNot { it.id == id }
    }

    val showBottomBar = currentBaseRoute != null &&
        currentBaseRoute !in AuthScreen.routes &&
        !isSelectHomeRoute &&
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
                                            popUpTo(navController.graph[Screen.Home.route].id)
                                            launchSingleTop = true
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
                    onAddHome = { /* TODO: add-home flow */ },
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
                    onNavigateHistory = {
                        navController.navigate(Screen.History.route) {
                            popUpTo(navController.graph[Screen.Home.route].id)
                            launchSingleTop = true
                        }
                    },
                    onNavigateNotifications = openNotifications,
                    unreadNotificationCount = unreadNotificationCount,
                )
            }
            composable(Screen.Rooms.route) {
                RoomsScreen(
                    windowSize = windowSize,
                    onProfileClick = {
                        navController.navigate(Screen.Profile.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateHome = {
                        navController.navigate(Screen.Home.route) {
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
                    onNavigateHistory = {
                        navController.navigate(Screen.History.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNotificationsClick = openNotifications,
                    unreadNotificationCount = unreadNotificationCount,
                )
            }
            composable(Screen.Routines.route) {
                RoutinesScreen(
                    onNotificationsClick = openNotifications,
                    unreadNotificationCount = unreadNotificationCount,
                    onProfileClick = {
                        navController.navigate(Screen.Profile.route) {
                            launchSingleTop = true
                        }
                    },
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
                    onOpenNotificationSettings = openNotificationSettings,
                    notificationPreferences = notificationPreferences,
                    onNotificationPreferencesChange = { notificationPreferences = it },
                    onNotificationsClick = openNotifications,
                    unreadNotificationCount = unreadNotificationCount,
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
                    onNavigateHistory = {
                        navController.navigate(Screen.History.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(
                    windowSize = windowSize,
                    onProfileClick = {
                        navController.navigate(Screen.Profile.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateHome = {
                        navController.navigate(Screen.Home.route) {
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
                    onNavigateRooms = {
                        navController.navigate(Screen.Rooms.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNotificationsClick = openNotifications,
                    unreadNotificationCount = unreadNotificationCount,
                )
            }
            composable(Screen.Notifications.route) {
                NotificationsScreen(
                    windowSize = windowSize,
                    notifications = notifications,
                    onNotificationClick = markNotificationRead,
                    onMarkAllRead = markAllNotificationsRead,
                    onAcceptInvitation = acceptInvitation,
                    onRejectInvitation = rejectInvitation,
                    onProfileClick = {
                        navController.navigate(Screen.Profile.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateHome = {
                        navController.navigate(Screen.Home.route) {
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
                    onNavigateRooms = {
                        navController.navigate(Screen.Rooms.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateHistory = {
                        navController.navigate(Screen.History.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable(Screen.NotificationSettings.route) {
                NotificationSettingsScreen(
                    windowSize = windowSize,
                    preferences = notificationPreferences,
                    onPreferencesChange = { notificationPreferences = it },
                    onProfileClick = {
                        navController.navigate(Screen.Profile.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateHome = {
                        navController.navigate(Screen.Home.route) {
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
                    onNavigateRooms = {
                        navController.navigate(Screen.Rooms.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateHistory = {
                        navController.navigate(Screen.History.route) {
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
                            onHistoryClick = {
                                navController.navigate(Screen.History.route) {
                                    popUpTo(navController.graph[Screen.Home.route].id)
                                    launchSingleTop = true
                                }
                            },
                        ),
                        onClose = closeDrawer,
                    )
                }
            }
        }
    }
}
