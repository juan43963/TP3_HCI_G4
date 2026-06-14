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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.window.core.layout.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
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
import com.grupo4.smarthome.ui.components.HomeSwitcherState
import com.grupo4.smarthome.ui.components.LocalHomeSwitcher
import com.grupo4.smarthome.ui.components.LocalOpenDrawer
import com.grupo4.smarthome.ui.components.QuickActionsOverlay
import com.grupo4.smarthome.ui.navigation.AuthScreen
import com.grupo4.smarthome.ui.screens.selecthome.SelectHomeViewModel
import com.grupo4.smarthome.ui.screens.selecthome.SelectHomeViewModelFactory
import com.grupo4.smarthome.ui.navigation.Screen
import com.grupo4.smarthome.ui.screens.dashboard.layout.phone.PhoneHomeDrawer
import com.grupo4.smarthome.ui.screens.firsthome.CreateFirstHomeScreen
import com.grupo4.smarthome.ui.screens.history.HistoryScreen
import com.grupo4.smarthome.ui.screens.history.HistoryViewModel
import com.grupo4.smarthome.ui.screens.history.HistoryViewModelFactory
import com.grupo4.smarthome.ui.screens.history.toHistoryEntries
import com.grupo4.smarthome.ui.screens.home.HomeScreen
import com.grupo4.smarthome.ui.screens.home.HomeViewModel
import com.grupo4.smarthome.ui.screens.home.HomeViewModelFactory
import com.grupo4.smarthome.ui.screens.selecthome.AddHomeOverlay
import com.grupo4.smarthome.ui.screens.selecthome.HomeActionDialogs
import com.grupo4.smarthome.ui.screens.notifications.NotificationSettingsScreen
import com.grupo4.smarthome.ui.screens.notifications.NotificationsScreen
import com.grupo4.smarthome.ui.screens.notifications.NotificationsViewModel
import com.grupo4.smarthome.ui.screens.notifications.NotificationsViewModelFactory
import com.grupo4.smarthome.ui.screens.notifications.toNotificationUi
import com.grupo4.smarthome.ui.screens.selecthome.SelectHomeScreen
import com.grupo4.smarthome.ui.screens.profile.ProfileScreen
import com.grupo4.smarthome.ui.screens.profile.ProfileViewModel
import com.grupo4.smarthome.ui.screens.profile.ProfileViewModelFactory
import com.grupo4.smarthome.ui.screens.rooms.RoomsScreen
import com.grupo4.smarthome.ui.screens.rooms.RoomsViewModel
import com.grupo4.smarthome.ui.screens.rooms.RoomsViewModelFactory
import com.grupo4.smarthome.ui.screens.rooms.ScannedDeviceRoomTarget
import com.grupo4.smarthome.ui.screens.rooms.dialogs.AddDevicesToRoomDialog
import com.grupo4.smarthome.ui.screens.rooms.dialogs.AddEditRoomDialog
import com.grupo4.smarthome.ui.screens.rooms.dialogs.AddScannedDeviceDialog
import com.grupo4.smarthome.data.model.QrDevicePayload
import com.grupo4.smarthome.ui.screens.device.DeviceDetailScreen
import com.grupo4.smarthome.ui.screens.scan.ScanNavEvent
import com.grupo4.smarthome.ui.screens.scan.ScanScreen
import com.grupo4.smarthome.ui.screens.scan.ScanViewModel
import com.grupo4.smarthome.ui.screens.scan.ScanViewModelFactory
import com.grupo4.smarthome.ui.screens.search.SearchCallbacks
import com.grupo4.smarthome.ui.screens.search.SearchScreen
import com.grupo4.smarthome.ui.screens.search.SearchViewModel
import com.grupo4.smarthome.ui.screens.routines.AddRoutineActionOverlay
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
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
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

private const val LOADING_HOME_ID = "\u0000__loading_home__"

// Permite que el dispositivo escaneado pendiente de alta sobreviva a la rotación / muerte de proceso
// vía rememberSaveable, sin volver Parcelable el modelo de dominio (se guarda como lista de strings).
private val QrDevicePayloadSaver = listSaver<QrDevicePayload?, String?>(
    save = { payload ->
        if (payload == null) emptyList()
        else listOf(payload.qrId, payload.typeName, payload.suggestedName)
    },
    restore = { stored ->
        if (stored.isEmpty()) null
        else QrDevicePayload(
            qrId = stored[0].orEmpty(),
            typeName = stored.getOrNull(1),
            suggestedName = stored.getOrNull(2),
        )
    },
)

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
    val isMainRoute = currentBaseRoute != null && currentBaseRoute !in AuthScreen.routes

    var isDrawerVisible by remember(currentBaseRoute) { mutableStateOf(false) }
    var showAddHome by rememberSaveable { mutableStateOf(false) }

    val homesApplication = LocalContext.current.applicationContext as SmartHomeApplication

    val sessionState by homesApplication.appContainer.sessionManager.sessionState
        .collectAsStateWithLifecycle()

    val loadingHomeId = LOADING_HOME_ID
    val storedHomeId by homesApplication.appContainer.preferencesDataStore.selectedHomeId
        .collectAsStateWithLifecycle(initialValue = loadingHomeId)
    val hasSelectedHome = storedHomeId != loadingHomeId && !storedHomeId.isNullOrBlank()

    if (sessionState is SessionState.Loading ||
        (sessionState is SessionState.Authenticated && storedHomeId == loadingHomeId)
    ) {
        Box(modifier = Modifier.fillMaxSize())
        return
    }
    val startDestination = when {
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
                homesApplication.appContainer.socketEvents,
                homesApplication.appContainer.preferencesDataStore,
            )
        },
    )
    val roomsUiState by roomsViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(selectHomeUiState.activeHomeId) {
        roomsViewModel.onHomeChanged(selectHomeUiState.activeHomeId)
    }

    val routinesViewModel: RoutinesViewModel = viewModel(
        factory = remember(homesApplication) {
            RoutinesViewModelFactory(
                homesApplication.appContainer.routinesRepository,
                homesApplication.appContainer.roomsRepository,
                homesApplication.appContainer.devicesRepository,
                homesApplication.appContainer.preferencesDataStore,
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

    val searchViewModel: SearchViewModel = viewModel()
    val searchUiState by searchViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(selectHomeUiState.activeHomeId) {
        searchViewModel.onHomeChanged(selectHomeUiState.activeHomeId)
    }

    val homeViewModel: HomeViewModel = viewModel(
        factory = remember(homesApplication) {
            HomeViewModelFactory(
                homesApplication.appContainer.roomsRepository,
                homesApplication.appContainer.devicesRepository,
                homesApplication.appContainer.routinesRepository,
                homesApplication.appContainer.preferencesDataStore,
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

    LaunchedEffect(
        currentBaseRoute,
        selectHomeUiState.homesLoaded,
        selectHomeUiState.homes.isEmpty(),
    ) {
        if (currentBaseRoute == Screen.SELECT_HOME_ROUTE &&
            selectHomeUiState.homesLoaded &&
            selectHomeUiState.errorMessage == null &&
            selectHomeUiState.homes.isEmpty()
        ) {
            navController.navigate(Screen.CREATE_FIRST_HOME_ROUTE) {
                popUpTo(Screen.SELECT_HOME_ROUTE) { inclusive = true }
            }
        }
    }

    LaunchedEffect(selectHomeUiState.homeJustCreated) {
        if (selectHomeUiState.homeJustCreated) {
            selectHomeViewModel.consumeHomeCreated()
            showAddHome = false
            if (currentBaseRoute == Screen.CREATE_FIRST_HOME_ROUTE) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.CREATE_FIRST_HOME_ROUTE) { inclusive = true }
                }
            }
        }
    }
    var showQuickActions by rememberSaveable { mutableStateOf(false) }
    var showCreateRoom by rememberSaveable { mutableStateOf(false) }
    var showCreateDevice by rememberSaveable { mutableStateOf(false) }
    var showCreateRoutine by rememberSaveable { mutableStateOf(false) }
    var routineActionTargetId by rememberSaveable { mutableStateOf<String?>(null) }
    var routineEditTargetId by rememberSaveable { mutableStateOf<String?>(null) }
    // Dispositivo escaneado pendiente de alta; rememberSaveable para sobrevivir rotación (igual que
    // el resto de overlays de creación). Abre AddScannedDeviceDialog.
    var scannedPayload by rememberSaveable(stateSaver = QrDevicePayloadSaver) {
        mutableStateOf<QrDevicePayload?>(null)
    }
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
    val openScanQr: () -> Unit = {
        showQuickActions = false
        navController.navigate(Screen.SCAN_QR_ROUTE) { launchSingleTop = true }
    }
    val openDrawer: () -> Unit = {
        if (isMainRoute) {
            selectHomeViewModel.loadHomes()
            isDrawerVisible = true
        }
    }
    val closeDrawer: () -> Unit = { isDrawerVisible = false }

    val performLogout: () -> Unit = {
        logoutScope.launch { authRepository.logout() }
        navController.navigate(AuthScreen.Welcome.route) {
            popUpTo(0) { inclusive = true }
        }
    }

    val notificationsViewModel: NotificationsViewModel = viewModel(
        factory = remember(homesApplication) {
            NotificationsViewModelFactory(
                homesApplication.appContainer.notificationsRepository,
                homesApplication.appContainer.preferencesDataStore,
            )
        },
    )
    val notificationsUiState by notificationsViewModel.uiState.collectAsStateWithLifecycle()
    val notificationPreferences by notificationsViewModel.preferences.collectAsStateWithLifecycle()
    LaunchedEffect(roomsUiState.rooms) {
        notificationsViewModel.onRoomsChanged(roomsUiState.rooms)
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(notificationsViewModel) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            notificationsViewModel.observeRealtime()
        }
    }
    LaunchedEffect(selectHomeViewModel) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            selectHomeViewModel.loadHomes()
        }
    }
    val unreadNotificationCount = notificationsUiState.unreadCount
    val openNotifications: () -> Unit = {
        navController.navigate(Screen.Notifications.route) { launchSingleTop = true }
    }
    val openNotificationSettings: () -> Unit = {
        navController.navigate(Screen.NotificationSettings.route) { launchSingleTop = true }
    }
    val openSearch: () -> Unit = {
        navController.navigate(Screen.Search.route) { launchSingleTop = true }
    }

    val showBottomBar = !isTablet &&
        !isDrawerVisible &&
        Screen.isMainDestination(currentBaseRoute)

    val onBack: () -> Unit = {
        if (!navController.navigateUp()) {
            navController.navigate(Screen.Home.route) {
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val homeSwitcherState = HomeSwitcherState(
        homes = selectHomeUiState.orderedDrawerHomes,
        activeHomeId = selectHomeUiState.activeHomeId,
        onHomeSelected = { selectHomeViewModel.selectHome(it) },
        onEditHome = { selectHomeViewModel.openRenameDialog(it) },
        onShareHome = { selectHomeViewModel.openShareDialog(it) },
        onAddHome = { showAddHome = true },
    )

    CompositionLocalProvider(
        LocalOpenDrawer provides openDrawer,
        LocalHomeSwitcher provides homeSwitcherState,
    ) {
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
                                val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
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
                    val target = if (hasSelectedHome) Screen.Home.route else Screen.SELECT_HOME_ROUTE
                    navController.navigate(target) {
                        popUpTo(AuthScreen.Welcome.route) { inclusive = true }
                    }
                    Unit
                }
                val loginContext = LocalContext.current
                val onNeedsVerification: (String, String) -> Unit = { email, password ->
                    Toast.makeText(
                        loginContext,
                        loginContext.getString(R.string.login_account_not_verified),
                        Toast.LENGTH_LONG,
                    ).show()
                    navController.navigate(
                        AuthScreen.VerifyAccount.createRoute(Uri.encode(email), Uri.encode(password))
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
                    navController.navigate(
                        AuthScreen.VerifyAccount.createRoute(Uri.encode(email), Uri.encode(password))
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
                    },
                    navArgument(AuthScreen.VerifyAccount.passwordArg) {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                ),
            ) { backStackEntry ->
                val email = backStackEntry.arguments
                    ?.getString(AuthScreen.VerifyAccount.emailArg)
                    .orEmpty()
                val password = backStackEntry.arguments
                    ?.getString(AuthScreen.VerifyAccount.passwordArg)
                    .orEmpty()
                val application = LocalContext.current.applicationContext as SmartHomeApplication
                val verifyFactory = remember(application) {
                    VerifyAccountViewModelFactory(application.appContainer.authRepository)
                }
                val verifyViewModel: VerifyAccountViewModel = viewModel(factory = verifyFactory)
                VerifyAccountScreen(
                    email = email,
                    password = password,
                    onVerifySuccess = {
                        navController.navigate(Screen.CREATE_FIRST_HOME_ROUTE) {
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

            composable(Screen.CREATE_FIRST_HOME_ROUTE) {
                CreateFirstHomeScreen(
                    windowSize = windowSize,
                    onCreateHome = { showAddHome = true },
                )
            }

            composable(Screen.SELECT_HOME_ROUTE) {
                SelectHomeScreen(
                    windowSize = windowSize,
                    userName = selectHomeUiState.userName,
                    homes = selectHomeUiState.homes,
                    isLoading = selectHomeUiState.isLoading,
                    homesLoaded = selectHomeUiState.homesLoaded,
                    errorMessage = selectHomeUiState.errorMessage?.let { stringResource(it) },
                    onRetry = selectHomeViewModel::loadHomes,
                    onHomeSelected = { home ->
                        selectHomeViewModel.selectHome(home.id)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.SELECT_HOME_ROUTE) { inclusive = true }
                        }
                    },
                    onAddHome = { showAddHome = true },
                    onSearchClick = openSearch,
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
                val dashboardSnackbarHostState = remember { SnackbarHostState() }
                val routineExecutedTemplate = stringResource(R.string.routines_executed)
                LaunchedEffect(routinesUiState.infoMessage) {
                    routinesUiState.infoMessage?.let {
                        dashboardSnackbarHostState.showSnackbar(routineExecutedTemplate.format(it))
                        routinesViewModel.dismissInfo()
                    }
                }
                Box(modifier = Modifier.fillMaxSize()) {
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
                    onSearchClick = openSearch,
                    onRoomClick = { roomId ->
                        roomsViewModel.onRoomOpenedFromDashboard(roomId)
                        navController.navigate(Screen.Rooms.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onToggleRoomFavorite = homeViewModel::toggleRoomFavorite,
                    onToggleRoutineFavorite = homeViewModel::toggleRoutineFavorite,
                    onRunRoutine = routinesViewModel::executeRoutine,
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
                SnackbarHost(
                    hostState = dashboardSnackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
                }
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    windowSize = windowSize,
                    state = searchUiState,
                    rooms = roomsUiState.rooms,
                    routines = routinesUiState.routines,
                    roomsErrorMessage = roomsUiState.errorMessage?.let { stringResource(it) },
                    deviceActionResult = roomsUiState.actionResult,
                    routinesErrorMessage = routinesUiState.errorMessage?.let { stringResource(it) },
                    routineInfoMessage = routinesUiState.infoMessage,
                    callbacks = SearchCallbacks(
                        onQueryChange = searchViewModel::onQueryChange,
                        onBack = { navController.popBackStack() },
                        onRoomClick = { roomId ->
                            roomsViewModel.onRoomClick(roomId)
                            navController.navigate(Screen.Rooms.route) {
                                launchSingleTop = true
                            }
                        },
                        onToggleRoomFavorite = roomsViewModel::toggleRoomFavorite,
                        onToggleDevice = roomsViewModel::onToggleDevice,
                        onDeviceValueChange = roomsViewModel::onDeviceValueChange,
                        onDeviceAction = roomsViewModel::runDeviceAction,
                        onRunRoutine = routinesViewModel::executeRoutine,
                        onToggleRoutineFavorite = routinesViewModel::toggleFavorite,
                        onDismissRoomError = roomsViewModel::dismissError,
                        onConsumeDeviceActionResult = roomsViewModel::consumeActionResult,
                        onDismissRoutineError = routinesViewModel::dismissError,
                        onDismissRoutineInfo = routinesViewModel::dismissInfo,
                    ),
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
                    onSearchClick = {
                        roomsViewModel.onSearchChange("")
                        openSearch()
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
                    onSearchClick = openSearch,
                    onProfileClick = {
                        navController.navigate(Screen.Profile.route) {
                            launchSingleTop = true
                        }
                    },
                    onCreateRoom = openCreateRoom,
                    onCreateDevice = openCreateDevice,
                    onAddRoutine = if (isTablet) openCreateRoutine else openQuickActions,
                    onCreateFirstRoutine = openCreateRoutine,
                    onQuickAddClick = openQuickActions,
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
                    onNavigateHistory = {
                        navController.navigate(Screen.History.route) {
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
                    onToggleRoutineFavorite = routinesViewModel::toggleFavorite,
                    onEditRoutine = { routineEditTargetId = it },
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
                        onAddAction = { routineActionTargetId = routineId },
                        onSave = { name, time24h, days, enabled ->
                            routinesViewModel.saveRoutine(routineId, name, time24h, days, enabled)
                            navController.popBackStack()
                        },
                    )
                }
            }
            composable(Screen.SCAN_QR_ROUTE) {
                val scanViewModel: ScanViewModel = viewModel(
                    factory = remember { ScanViewModelFactory() },
                )
                val scanState by scanViewModel.uiState.collectAsStateWithLifecycle()
                val scanNavEvent by scanViewModel.navEvent.collectAsStateWithLifecycle()
                val homeDevices = remember(roomsUiState.rooms) {
                    roomsUiState.rooms.flatMap { it.devices }
                }
                val devicesLoaded = roomsUiState.devicesLoaded
                // El match exige los devices del hogar cargados; si todavía no se cargaron, los pedimos al
                // entrar. También aseguramos el catálogo de tipos (lo usa el alta de un QR nuevo).
                LaunchedEffect(Unit) {
                    if (!roomsUiState.devicesLoaded && !roomsUiState.isLoading) roomsViewModel.loadRooms()
                    if (roomsUiState.availableDeviceTypes.isEmpty()) roomsViewModel.refreshDeviceTypes()
                }
                // Cuando los devices llegan (o termina la carga), reintentamos resolver un QR que haya
                // quedado pendiente por haberse leído durante la carga inicial.
                LaunchedEffect(homeDevices, devicesLoaded) {
                    scanViewModel.onDevicesUpdated(homeDevices, devicesLoaded)
                }
                LaunchedEffect(scanNavEvent) {
                    when (val event = scanNavEvent) {
                        is ScanNavEvent.OpenDevice -> {
                            scanViewModel.consumeNavEvent()
                            navController.navigate("${Screen.DEVICE_DETAIL_ROUTE}/${event.deviceId}") {
                                popUpTo(Screen.SCAN_QR_ROUTE) { inclusive = true }
                            }
                        }
                        is ScanNavEvent.CreateDevice -> {
                            scanViewModel.consumeNavEvent()
                            scannedPayload = event.payload
                            navController.popBackStack()
                        }
                        null -> Unit
                    }
                }
                ScanScreen(
                    uiState = scanState,
                    homeDevices = homeDevices,
                    onCodeDetected = { raw, devices ->
                        scanViewModel.onCodeDetected(raw, devices, devicesLoaded)
                    },
                    onClose = { navController.popBackStack() },
                    onRetry = scanViewModel::resumeScanning,
                )
            }
            composable("${Screen.DEVICE_DETAIL_ROUTE}/{deviceId}") { backStackEntry ->
                val deviceId = backStackEntry.arguments?.getString("deviceId").orEmpty()
                val device = roomsUiState.rooms
                    .flatMap { it.devices }
                    .firstOrNull { it.id == deviceId }
                if (device == null) {
                    LaunchedEffect(deviceId) { navController.popBackStack() }
                } else {
                    val roomName = roomsUiState.rooms.firstOrNull { it.id == device.roomId }?.name
                    DeviceDetailScreen(
                        device = device,
                        roomName = roomName,
                        isTablet = isTablet,
                        onBack = { navController.popBackStack() },
                        onToggle = { roomsViewModel.onToggleDevice(device.id) },
                        onValueChange = { value -> roomsViewModel.onDeviceValueChange(device.id, value) },
                        onAction = { name, args -> roomsViewModel.runDeviceAction(device.id, name, args) },
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
                    onLogout = performLogout,
                    onOpenNotificationSettings = openNotificationSettings,
                    notificationPreferences = notificationPreferences,
                    onNotificationPreferencesChange = notificationsViewModel::setPreferences,
                    onNotificationsClick = openNotifications,
                    onSearchClick = openSearch,
                    unreadNotificationCount = unreadNotificationCount,
                    enteredFromHome = enteredFromHome,
                    onBack = onBack,
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
                    onBack = onBack,
                    selectedHomeName = activeHomeName,
                    onSearchClick = openSearch,
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
                val context = LocalContext.current
                val notificationItems = remember(notificationsUiState.items, roomsUiState.rooms) {
                    notificationsUiState.items.toNotificationUi(context, roomsUiState.rooms)
                }
                DisposableEffect(Unit) {
                    notificationsViewModel.onScreenVisible()
                    onDispose { notificationsViewModel.onScreenHidden() }
                }
                NotificationsScreen(
                    windowSize = windowSize,
                    notifications = notificationItems,
                    onMarkAllRead = notificationsViewModel::markAllSeen,
                    onBack = onBack,
                    selectedHomeName = activeHomeName,
                    onHomeSelectorClick = openDrawer,
                    onSearchClick = openSearch,
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
                    onPreferencesChange = notificationsViewModel::setPreferences,
                    onBack = onBack,
                    onSearchClick = openSearch,
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
                        onLogout = performLogout,
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
                onCreate = { name, typeIndex, shareEmails ->
                    selectHomeViewModel.createHome(name, typeIndex, shareEmails)
                },
            )

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
                onScanQr = openScanQr,
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
                        rooms = roomsUiState.rooms,
                        initialRoomId = targetRoom.id,
                        availableDeviceTypes = roomsUiState.availableDeviceTypes,
                        onDismiss = { showCreateDevice = false },
                        onConfirm = { roomId, pendingDevices ->
                            roomsViewModel.addDevices(roomId, pendingDevices)
                            showCreateDevice = false
                        },
                    )
                }
            }

            val scanned = scannedPayload
            if (scanned != null) {
                // El alta de un QR nuevo necesita el catálogo de tipos; si quedó vacío (p. ej. falló el
                // GET inicial), lo reintentamos al abrir el diálogo para no dejar al usuario sin opciones.
                LaunchedEffect(Unit) {
                    if (roomsUiState.availableDeviceTypes.isEmpty()) roomsViewModel.refreshDeviceTypes()
                }
                AddScannedDeviceDialog(
                    payload = scanned,
                    rooms = roomsUiState.rooms,
                    availableDeviceTypes = roomsUiState.availableDeviceTypes,
                    onDismiss = { scannedPayload = null },
                    onConfirm = { target, type, name ->
                        roomsViewModel.addScannedDevice(target, type, name, scanned.qrId)
                        scannedPayload = null
                    },
                )
            }

            val routineIsTablet = windowSize.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
            val routineEditTarget = routinesUiState.routines.firstOrNull {
                it.id == routineEditTargetId
            }
            if (routineIsTablet) {
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
                AddRoutineTabletOverlay(
                    visible = routineEditTarget != null,
                    onDismiss = { routineEditTargetId = null },
                    devices = routinesUiState.devices,
                    deviceRoomNames = routinesUiState.deviceRoomNames,
                    deviceActions = routinesUiState.deviceActions,
                    initialRoutine = routineEditTarget,
                    onCreate = { name, category, time24h, days, actions ->
                        routineEditTarget?.let { original ->
                            routinesViewModel.updateRoutine(
                                original.copy(
                                    name = name,
                                    category = category,
                                    time = time24h,
                                    days = days,
                                    actions = actions,
                                ),
                            )
                        }
                        routineEditTargetId = null
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

            AddRoutineActionOverlay(
                visible = routineActionTargetId != null && !routineIsTablet,
                onDismiss = { routineActionTargetId = null },
                devices = routinesUiState.devices,
                deviceRoomNames = routinesUiState.deviceRoomNames,
                deviceActions = routinesUiState.deviceActions,
                onAddAction = { action ->
                    routineActionTargetId?.let { routineId ->
                        routinesViewModel.addActionToRoutine(routineId, action)
                    }
                },
            )
        }
    }
}
