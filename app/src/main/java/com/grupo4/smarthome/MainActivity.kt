package com.grupo4.smarthome

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.window.core.layout.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grupo4.smarthome.data.local.SessionState
import kotlinx.coroutines.launch
import androidx.navigation.NavType
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.get
import com.grupo4.smarthome.ui.auth.change.AuthChangePasswordScreen
import com.grupo4.smarthome.ui.auth.change.AuthChangePasswordScreenTablet
import com.grupo4.smarthome.ui.auth.change.AuthChangePasswordViewModel
import com.grupo4.smarthome.ui.auth.change.AuthChangePasswordViewModelFactory
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
import com.grupo4.smarthome.ui.components.AppDrawerOverlay
import com.grupo4.smarthome.ui.components.LocalOpenDrawer
import com.grupo4.smarthome.ui.components.QuickActionsOverlay
import com.grupo4.smarthome.ui.navigation.AuthScreen
import com.grupo4.smarthome.ui.screens.selecthome.SelectHomeViewModel
import com.grupo4.smarthome.ui.screens.selecthome.SelectHomeViewModelFactory
import com.grupo4.smarthome.ui.navigation.Screen
import com.grupo4.smarthome.ui.screens.dashboard.layout.phone.PhoneHomeDrawer
import com.grupo4.smarthome.ui.screens.history.HistoryScreen
import com.grupo4.smarthome.ui.screens.history.HistoryViewModel
import com.grupo4.smarthome.ui.screens.history.HistoryViewModelFactory
import com.grupo4.smarthome.ui.screens.history.toHistoryEntries
import com.grupo4.smarthome.ui.screens.home.HomeScreen
import com.grupo4.smarthome.ui.screens.home.HomeViewModel
import com.grupo4.smarthome.ui.screens.home.HomeViewModelFactory
import com.grupo4.smarthome.ui.screens.selecthome.AddHomeOverlay
import com.grupo4.smarthome.ui.screens.selecthome.HomeActionDialogs
import com.grupo4.smarthome.ui.screens.notifications.NotificationPreferences
import com.grupo4.smarthome.ui.screens.notifications.NotificationPreferencesSaver
import com.grupo4.smarthome.ui.screens.notifications.NotificationSettingsScreen
import com.grupo4.smarthome.ui.screens.notifications.NotificationsScreen
import com.grupo4.smarthome.ui.screens.notifications.sampleNotifications
import com.grupo4.smarthome.ui.screens.selecthome.SelectHomeScreen
import com.grupo4.smarthome.ui.screens.profile.ProfileScreen
import com.grupo4.smarthome.ui.screens.profile.ProfileViewModel
import com.grupo4.smarthome.ui.screens.profile.ProfileViewModelFactory
import com.grupo4.smarthome.ui.screens.rooms.RoomsScreen
import com.grupo4.smarthome.ui.screens.rooms.RoomsViewModel
import com.grupo4.smarthome.ui.screens.rooms.RoomsViewModelFactory
import com.grupo4.smarthome.ui.screens.rooms.dialogs.AddDevicesToRoomDialog
import com.grupo4.smarthome.ui.screens.rooms.dialogs.AddEditRoomDialog
import com.grupo4.smarthome.ui.screens.routines.AddRoutineTabletOverlay
import com.grupo4.smarthome.ui.screens.routines.CreateRoutineOverlay
import com.grupo4.smarthome.ui.screens.routines.RoutineDetailScreen
import com.grupo4.smarthome.ui.screens.routines.RoutinesScreen
import com.grupo4.smarthome.ui.screens.routines.RoutinesViewModel
import com.grupo4.smarthome.ui.screens.routines.RoutinesViewModelFactory
import com.grupo4.smarthome.ui.screens.routines.deviceNamesById
import com.grupo4.smarthome.ui.screens.routines.deviceTypesById
import com.grupo4.smarthome.ui.screens.routines.toDetailUi
import com.grupo4.smarthome.ui.screens.routines.toRoutineUi
import com.grupo4.smarthome.ui.theme.SmartHomeTheme
import com.grupo4.smarthome.ui.theme.ThemePreference
import com.grupo4.smarthome.ui.theme.ThemeViewModel
import com.grupo4.smarthome.ui.theme.ThemeViewModelFactory
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        // Mantener el splash hasta resolver si hay sesión guardada o no
        val sessionManager = (application as SmartHomeApplication).appContainer.sessionManager
        splashScreen.setKeepOnScreenCondition {
            sessionManager.sessionState.value is SessionState.Loading
        }
        enableEdgeToEdge()
        setContent {
            val appContainer = (application as SmartHomeApplication).appContainer
            val themeViewModel: ThemeViewModel = viewModel(
                factory = remember { ThemeViewModelFactory(appContainer.preferencesDataStore) },
            )
            val themePreference by themeViewModel.themePreference.collectAsStateWithLifecycle()
            SmartHomeTheme(themePreference = themePreference) {
                val windowSize = currentWindowAdaptiveInfo().windowSizeClass
                SmartHomeApp(
                    windowSize = windowSize,
                    themePreference = themePreference,
                    onThemePreferenceChange = themeViewModel::setTheme,
                )
            }
        }
    }
}

// Sentinela para `selectedHomeId`: marca que la preferencia de casa activa todavía no se cargó
// desde DataStore (distinto de `null` = no hay casa seleccionada).
private const val LOADING_HOME_ID = " __loading_home__"

@Composable
fun SmartHomeApp(
    windowSize: WindowSizeClass,
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    onThemePreferenceChange: (ThemePreference) -> Unit = {},
) {
    val isTablet = windowSize.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val currentBaseRoute = currentRoute?.substringBefore('?')
    val isSelectHomeRoute = currentBaseRoute == Screen.SELECT_HOME_ROUTE
    val isRoutineDetailRoute = currentBaseRoute?.startsWith(Screen.ROUTINE_DETAIL_ROUTE) == true
    val isChangePasswordRoute = currentBaseRoute == Screen.CHANGE_PASSWORD_ROUTE
    val isMainRoute = currentBaseRoute != null && currentBaseRoute !in AuthScreen.routes

    var isDrawerVisible by remember(currentBaseRoute) { mutableStateOf(false) }
    var showAddHome by rememberSaveable { mutableStateOf(false) }

    // ViewModel de casas a nivel Activity: lo comparten SelectHome y el AddHomeOverlay (fuera del NavHost)
    val homesApplication = LocalContext.current.applicationContext as SmartHomeApplication

    // La pantalla de arranque depende de si hay sesión guardada; mientras carga no componemos nada
    val sessionState by homesApplication.appContainer.sessionManager.sessionState
        .collectAsStateWithLifecycle()

    // Casa seleccionada persistida (DataStore). El sentinela distingue "todavía no cargó la
    // preferencia" de "no hay casa seleccionada" (null), para elegir bien el destino inicial.
    val loadingHomeId = LOADING_HOME_ID
    val storedHomeId by homesApplication.appContainer.preferencesDataStore.selectedHomeId
        .collectAsStateWithLifecycle(initialValue = loadingHomeId)
    val hasSelectedHome = storedHomeId != loadingHomeId && !storedHomeId.isNullOrBlank()

    // Esperamos a resolver sesión y, si está autenticado, también la preferencia de casa activa.
    if (sessionState is SessionState.Loading ||
        (sessionState is SessionState.Authenticated && storedHomeId == loadingHomeId)
    ) {
        Box(modifier = Modifier.fillMaxSize())
        return
    }
    val startDestination = when {
        // Si ya hay una casa seleccionada, entramos directo al dashboard (no a la gestión de hogares)
        sessionState is SessionState.Authenticated && hasSelectedHome -> Screen.Home.route
        sessionState is SessionState.Authenticated -> Screen.SELECT_HOME_ROUTE
        else -> AuthScreen.Welcome.route
    }

    val logoutScope = rememberCoroutineScope()
    val authRepository = homesApplication.appContainer.authRepository

    val selectHomeViewModel: SelectHomeViewModel = viewModel(
        factory = remember(homesApplication) {
            SelectHomeViewModelFactory(
                homesApplication.appContainer.homesRepository,
                homesApplication.appContainer.sessionManager,
                homesApplication.appContainer.preferencesDataStore,
            )
        },
    )
    val selectHomeUiState by selectHomeViewModel.uiState.collectAsStateWithLifecycle()
    val activeHome = selectHomeUiState.homes.firstOrNull { it.id == selectHomeUiState.activeHomeId }
    val activeHomeName = activeHome?.name.orEmpty()

    val roomsViewModel: RoomsViewModel = viewModel(
        factory = remember(homesApplication) {
            RoomsViewModelFactory(
                homesApplication.appContainer.roomsRepository,
                homesApplication.appContainer.devicesRepository,
            )
        },
    )
    val roomsUiState by roomsViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(selectHomeUiState.activeHomeId) {
        roomsViewModel.onHomeChanged(selectHomeUiState.activeHomeId)
    }

    // ViewModel de rutinas a nivel Activity: lo comparten lista, detalle y overlays de creación
    val routinesViewModel: RoutinesViewModel = viewModel(
        factory = remember(homesApplication) {
            RoutinesViewModelFactory(
                homesApplication.appContainer.routinesRepository,
                homesApplication.appContainer.roomsRepository,
                homesApplication.appContainer.devicesRepository,
            )
        },
    )
    val routinesUiState by routinesViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(selectHomeUiState.activeHomeId) {
        routinesViewModel.onHomeChanged(selectHomeUiState.activeHomeId)
    }
    LaunchedEffect(routinesUiState.infoMessage) {
        if (routinesUiState.infoMessage != null) {
            roomsViewModel.loadRooms()
        }
    }

    val homeViewModel: HomeViewModel = viewModel(
        factory = remember(homesApplication) {
            HomeViewModelFactory(
                homesApplication.appContainer.roomsRepository,
                homesApplication.appContainer.devicesRepository,
                homesApplication.appContainer.routinesRepository,
            )
        },
    )
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(selectHomeUiState.activeHomeId, activeHomeName, selectHomeUiState.userName) {
        homeViewModel.onHomeChanged(
            newHomeId = selectHomeUiState.activeHomeId,
            homeName = activeHomeName,
            userName = selectHomeUiState.userName,
        )
    }
    LaunchedEffect(currentBaseRoute, selectHomeUiState.activeHomeId) {
        if (currentBaseRoute == Screen.Home.route && selectHomeUiState.activeHomeId != null) {
            homeViewModel.refresh(activeHomeName, selectHomeUiState.userName)
        }
    }
    LaunchedEffect(roomsUiState.rooms.size, roomsUiState.rooms.sumOf { it.devices.size }) {
        if (currentBaseRoute == Screen.Home.route && selectHomeUiState.activeHomeId != null) {
            homeViewModel.refresh(activeHomeName, selectHomeUiState.userName)
        }
    }

    LaunchedEffect(selectHomeUiState.homeJustCreated) {
        if (selectHomeUiState.homeJustCreated) {
            selectHomeViewModel.consumeHomeCreated()
            showAddHome = false
        }
    }
    var showQuickActions by rememberSaveable { mutableStateOf(false) }
    var showCreateRoom by rememberSaveable { mutableStateOf(false) }
    var showCreateDevice by rememberSaveable { mutableStateOf(false) }
    var showCreateRoutine by rememberSaveable { mutableStateOf(false) }
    val openQuickActions: () -> Unit = { showQuickActions = true }
    val openCreateRoom: () -> Unit = {
        showQuickActions = false
        showCreateDevice = false
        showCreateRoutine = false
        showCreateRoom = true
    }
    val openCreateDevice: () -> Unit = {
        showQuickActions = false
        showCreateRoom = false
        showCreateRoutine = false
        showCreateDevice = true
    }
    val openCreateRoutine: () -> Unit = {
        showQuickActions = false
        showCreateRoom = false
        showCreateDevice = false
        showCreateRoutine = true
    }
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
        !isRoutineDetailRoute &&
        !isChangePasswordRoute &&
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
                    startDestination = startDestination,
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
                    // Si ya había una casa seleccionada persistida, vamos directo al dashboard;
                    // si no, a la selección/gestión de hogares para elegir o crear una.
                    val target = if (hasSelectedHome) Screen.Home.route else Screen.SELECT_HOME_ROUTE
                    navController.navigate(target) {
                        popUpTo(AuthScreen.Welcome.route) { inclusive = true }
                    }
                    Unit
                }
                if (isTablet) {
                    LoginScreenTablet(
                        onNavigateToRegister = onNavigateToRegister,
                        onNavigateToForgotPassword = onNavigateToForgotPassword,
                        onLoginSuccess = onLoginSuccess,
                        viewModel = loginViewModel,
                    )
                } else {
                    LoginScreen(
                        onNavigateToRegister = onNavigateToRegister,
                        onNavigateToForgotPassword = onNavigateToForgotPassword,
                        onLoginSuccess = onLoginSuccess,
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
                val onRegisterSuccess: (String) -> Unit = { email ->
                    navController.navigate(AuthScreen.VerifyAccount.createRoute(Uri.encode(email)))
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
                val application = LocalContext.current.applicationContext as SmartHomeApplication
                val verifyFactory = remember(application) {
                    VerifyAccountViewModelFactory(application.appContainer.authRepository)
                }
                val verifyViewModel: VerifyAccountViewModel = viewModel(factory = verifyFactory)
                VerifyAccountScreen(
                    email = email,
                    onVerifySuccess = {
                        navController.navigate(AuthScreen.Login.route) {
                            popUpTo(AuthScreen.Welcome.route) { inclusive = true }
                        }
                    },
                    onChangeEmail = { navController.popBackStack() },
                    onBackToLogin = {
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

            composable(Screen.SELECT_HOME_ROUTE) {
                SelectHomeScreen(
                    windowSize = windowSize,
                    userName = selectHomeUiState.userName,
                    homes = selectHomeUiState.homes,
                    isLoading = selectHomeUiState.isLoading,
                    errorMessage = selectHomeUiState.errorMessage?.let { stringResource(it) },
                    onRetry = selectHomeViewModel::loadHomes,
                    onHomeSelected = { home ->
                        selectHomeViewModel.selectHome(home.id)
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
                    onShareHome = selectHomeViewModel::openShareDialog,
                    onRenameHome = selectHomeViewModel::openRenameDialog,
                    onDeleteHome = selectHomeViewModel::openDeleteDialog,
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    windowSize = windowSize,
                    state = homeUiState,
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
                    onQuickActionsOpen = openQuickActions,
                    onCreateRoom = openCreateRoom,
                    onCreateDevice = openCreateDevice,
                    onCreateRoutine = openCreateRoutine,
                    showQuickAddFab = !isTablet && !showQuickActions && !showCreateRoom &&
                        !showCreateDevice && !showCreateRoutine,
                )
            }
            composable(Screen.Rooms.route) {
                RoomsScreen(
                    viewModel = roomsViewModel,
                    activeHomeId = selectHomeUiState.activeHomeId,
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
                    onQuickActionsOpen = openQuickActions,
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
                    onCreateRoom = openCreateRoom,
                    onCreateDevice = openCreateDevice,
                    onAddRoutine = if (isTablet) openCreateRoutine else openQuickActions,
                    showQuickAddFab = !isTablet && !showQuickActions && !showCreateRoom &&
                        !showCreateDevice && !showCreateRoutine,
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
                    routines = routinesUiState.routines.map {
                        it.toRoutineUi(routinesUiState.deviceNamesById)
                    },
                    isLoading = routinesUiState.isLoading,
                    errorMessage = routinesUiState.errorMessage?.let { stringResource(it) },
                    infoMessage = routinesUiState.infoMessage,
                    onExecuteRoutine = routinesViewModel::executeRoutine,
                    onSaveRoutine = { id, name, time24h, days, enabled ->
                        routinesViewModel.saveRoutine(id, name, time24h, days, enabled)
                    },
                    onDeleteRoutine = routinesViewModel::deleteRoutine,
                    detailFor = { id ->
                        routinesUiState.routines.firstOrNull { it.id == id }?.toDetailUi(
                            routinesUiState.deviceNamesById,
                            routinesUiState.deviceTypesById,
                        )
                    },
                    onRetry = routinesViewModel::loadRoutines,
                    onDismissError = routinesViewModel::dismissError,
                    onDismissInfo = routinesViewModel::dismissInfo,
                )
            }
            composable("${Screen.ROUTINE_DETAIL_ROUTE}/{routineId}") { backStackEntry ->
                val routineId = backStackEntry.arguments?.getString("routineId").orEmpty()
                val routine = routinesUiState.routines.firstOrNull { it.id == routineId }
                if (routine == null) {
                    // La rutina ya no existe (p.ej. tras borrarla): volvemos al listado
                    LaunchedEffect(routineId) { navController.popBackStack() }
                } else {
                    RoutineDetailScreen(
                        routine = routine.toDetailUi(
                            routinesUiState.deviceNamesById,
                            routinesUiState.deviceTypesById,
                        ),
                        onBack = { navController.popBackStack() },
                        onDelete = {
                            routinesViewModel.deleteRoutine(routineId)
                            navController.popBackStack()
                        },
                        onSave = { name, time24h, days, enabled ->
                            routinesViewModel.saveRoutine(routineId, name, time24h, days, enabled)
                            navController.popBackStack()
                        },
                    )
                }
            }
            composable(Screen.Profile.route) {
                val origin = navController.previousBackStackEntry
                    ?.destination?.route?.substringBefore('?')
                val enteredFromHome = origin != null && origin != Screen.SELECT_HOME_ROUTE
                val context = LocalContext.current
                val profileFactory = remember(homesApplication) {
                    ProfileViewModelFactory(
                        homesApplication.appContainer.profileRepository,
                        homesApplication.appContainer.sessionManager,
                    )
                }
                val profileViewModel: ProfileViewModel = viewModel(factory = profileFactory)
                val profileUiState by profileViewModel.uiState.collectAsStateWithLifecycle()
                LaunchedEffect(profileUiState.nameSaved) {
                    if (profileUiState.nameSaved) {
                        profileViewModel.consumeNameSaved()
                        Toast.makeText(
                            context,
                            context.getString(R.string.profile_saved),
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
                ProfileScreen(
                    windowSize = windowSize,
                    userName = profileUiState.name,
                    userEmail = profileUiState.email,
                    isSavingName = profileUiState.isSaving,
                    onSaveName = profileViewModel::saveName,
                    themePreference = themePreference,
                    onThemePreferenceChange = onThemePreferenceChange,
                    onChangePassword = {
                        navController.navigate(Screen.CHANGE_PASSWORD_ROUTE)
                    },
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
            composable(Screen.CHANGE_PASSWORD_ROUTE) {
                val application = LocalContext.current.applicationContext as SmartHomeApplication
                val context = LocalContext.current
                val authChangeFactory = remember(application) {
                    AuthChangePasswordViewModelFactory(application.appContainer.authRepository)
                }
                val authChangeViewModel: AuthChangePasswordViewModel =
                    viewModel(factory = authChangeFactory)
                val onPasswordChanged: () -> Unit = {
                    Toast.makeText(
                        context,
                        context.getString(R.string.auth_change_pwd_success),
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
            composable(Screen.History.route) {
                val context = LocalContext.current
                val historyFactory = remember(homesApplication) {
                    HistoryViewModelFactory(homesApplication.appContainer.devicesRepository)
                }
                val historyViewModel: HistoryViewModel = viewModel(factory = historyFactory)
                val historyUiState by historyViewModel.uiState.collectAsStateWithLifecycle()
                // Cruzamos los logs con el catálogo de dispositivos del hogar activo para mostrar
                // nombre/habitación/icono en vez del deviceId crudo.
                val historyEntries = remember(historyUiState.logs, roomsUiState.rooms) {
                    historyUiState.logs.toHistoryEntries(context, roomsUiState.rooms)
                }
                HistoryScreen(
                    windowSize = windowSize,
                    entries = historyEntries,
                    isLoading = historyUiState.isLoading,
                    isLoadingMore = historyUiState.isLoadingMore,
                    canLoadMore = historyUiState.canLoadMore,
                    onLoadMore = historyViewModel::loadMore,
                    errorMessage = historyUiState.errorMessage?.let { stringResource(it) },
                    onRefresh = historyViewModel::load,
                    onRetry = historyViewModel::load,
                    selectedHomeName = activeHomeName,
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
                    onQuickAddClick = openQuickActions,
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
                        homes = selectHomeUiState.orderedDrawerHomes,
                        activeHomeId = selectHomeUiState.activeHomeId,
                        onHomeSelected = { homeId ->
                            selectHomeViewModel.selectHome(homeId)
                        },
                        onEditHome = { home ->
                            selectHomeViewModel.openRenameDialog(home)
                            closeDrawer()
                        },
                        onShareHome = { home ->
                            selectHomeViewModel.openShareDialog(home)
                            closeDrawer()
                        },
                        onAddHome = {
                            showAddHome = true
                        },
                        onHistory = {
                            navController.navigate(Screen.History.route) {
                                popUpTo(navController.graph[Screen.Home.route].id)
                                launchSingleTop = true
                            }
                        },
                        onLogout = {
                            logoutScope.launch { authRepository.logout() }
                            navController.navigate(AuthScreen.Welcome.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onClose = closeDrawer,
                    )
                }
            }

            AddHomeOverlay(
                visible = showAddHome,
                isTablet = isTablet,
                onDismiss = { showAddHome = false },
                isCreating = selectHomeUiState.isCreating,
                errorMessage = selectHomeUiState.createError?.let { stringResource(it) },
                onCreate = { name, typeIndex, photoIndex, shareEmail ->
                    selectHomeViewModel.createHome(name, typeIndex, photoIndex, shareEmail)
                },
            )

            // Diálogos de editar/compartir/eliminar hogar: render único a nivel Activity, driven por el
            // SelectHomeViewModel compartido. Funcionan desde SelectHomeScreen (tablet) y desde el drawer
            // de teléfono sin duplicarse.
            HomeActionDialogs(
                dialog = selectHomeUiState.dialog,
                isProcessing = selectHomeUiState.isProcessingAction,
                actionError = selectHomeUiState.actionError?.let { stringResource(it) },
                sharedUsers = selectHomeUiState.sharedUsers,
                isLoadingShared = selectHomeUiState.isLoadingShared,
                onDismiss = selectHomeViewModel::dismissDialog,
                onConfirmRename = selectHomeViewModel::renameHome,
                onConfirmDelete = selectHomeViewModel::deleteHome,
                onConfirmShare = selectHomeViewModel::shareHome,
            )

            QuickActionsOverlay(
                visible = showQuickActions,
                onDismiss = { showQuickActions = false },
                onAddRoom = openCreateRoom,
                onAddDevice = openCreateDevice,
                onAddRoutine = openCreateRoutine,
                // Lift the overlay above the bottom NavigationBar (M3 default height
                // 80.dp) so its close FAB stays where the "+" FAB was.
                bottomBarPadding = if (showBottomBar) 80.dp else 0.dp,
            )

            if (showCreateRoom) {
                AddEditRoomDialog(
                    room = null,
                    onDismiss = { showCreateRoom = false },
                    onConfirm = { name, type ->
                        roomsViewModel.createRoom(name, type)
                        showCreateRoom = false
                    },
                )
            }

            val targetRoom = roomsUiState.rooms.firstOrNull { it.id == roomsUiState.selectedRoomId }
                ?: roomsUiState.rooms.firstOrNull()
            if (showCreateDevice) {
                if (targetRoom == null) {
                    AddEditRoomDialog(
                        room = null,
                        onDismiss = { showCreateDevice = false },
                        onConfirm = { name, type ->
                            roomsViewModel.createRoom(name, type)
                            showCreateDevice = false
                        },
                    )
                } else {
                    AddDevicesToRoomDialog(
                        roomName = targetRoom.name,
                        availableDeviceTypes = roomsUiState.availableDeviceTypes,
                        onDismiss = { showCreateDevice = false },
                        onConfirm = { pendingDevices ->
                            roomsViewModel.addDevices(targetRoom.id, pendingDevices)
                            showCreateDevice = false
                        },
                    )
                }
            }

            if (isTablet) {
                AddRoutineTabletOverlay(
                    visible = showCreateRoutine,
                    onDismiss = { showCreateRoutine = false },
                    devices = routinesUiState.devices,
                    deviceRoomNames = routinesUiState.deviceRoomNames,
                    deviceActions = routinesUiState.deviceActions,
                    onCreate = { name, category, time24h, days, actions ->
                        routinesViewModel.createRoutine(name, category, time24h, days, actions)
                    },
                )
            } else {
                CreateRoutineOverlay(
                    visible = showCreateRoutine,
                    isTablet = false,
                    onDismiss = { showCreateRoutine = false },
                    devices = routinesUiState.devices,
                    deviceRoomNames = routinesUiState.deviceRoomNames,
                    deviceActions = routinesUiState.deviceActions,
                    onCreate = { name, category, time24h, days, actions ->
                        routinesViewModel.createRoutine(name, category, time24h, days, actions)
                    },
                )
            }
        }
    }
}
