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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grupo4.smarthome.data.local.SessionState
import kotlinx.coroutines.launch
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.get
import com.grupo4.smarthome.ui.components.AppDrawerOverlay
import com.grupo4.smarthome.ui.components.HomeSwitcherState
import com.grupo4.smarthome.ui.components.LocalHomeSwitcher
import com.grupo4.smarthome.ui.components.LocalOpenDrawer
import com.grupo4.smarthome.ui.components.QuickActionsOverlay
import com.grupo4.smarthome.ui.auth.AuthFlowViewModel
import com.grupo4.smarthome.ui.navigation.AuthScreen
import com.grupo4.smarthome.ui.navigation.accountGraph
import com.grupo4.smarthome.ui.navigation.authGraph
import com.grupo4.smarthome.ui.navigation.MainActions
import com.grupo4.smarthome.ui.navigation.mainGraph
import com.grupo4.smarthome.ui.navigation.selectHomeGraph
import com.grupo4.smarthome.ui.screens.selecthome.SelectHomeViewModel
import com.grupo4.smarthome.ui.screens.selecthome.SelectHomeViewModelFactory
import com.grupo4.smarthome.ui.navigation.Screen
import com.grupo4.smarthome.ui.screens.dashboard.layout.phone.PhoneHomeDrawer
import com.grupo4.smarthome.ui.screens.home.HomeViewModel
import com.grupo4.smarthome.ui.screens.home.HomeViewModelFactory
import com.grupo4.smarthome.ui.screens.selecthome.AddHomeOverlay
import com.grupo4.smarthome.ui.screens.selecthome.HomeActionDialogs
import com.grupo4.smarthome.ui.screens.notifications.NotificationsViewModel
import com.grupo4.smarthome.ui.screens.notifications.NotificationsViewModelFactory
import com.grupo4.smarthome.ui.screens.rooms.RoomsViewModel
import com.grupo4.smarthome.ui.screens.rooms.RoomsViewModelFactory
import com.grupo4.smarthome.ui.screens.rooms.dialogs.AddDevicesToRoomDialog
import com.grupo4.smarthome.ui.screens.rooms.dialogs.AddEditRoomDialog
import com.grupo4.smarthome.ui.screens.rooms.dialogs.AddScannedDeviceDialog
import com.grupo4.smarthome.data.model.QrDevicePayload
import com.grupo4.smarthome.ui.screens.search.SearchViewModel
import com.grupo4.smarthome.ui.screens.routines.AddRoutineActionOverlay
import com.grupo4.smarthome.ui.screens.routines.AddRoutineTabletOverlay
import com.grupo4.smarthome.ui.screens.routines.CreateRoutineOverlay
import com.grupo4.smarthome.ui.screens.routines.RoutinesViewModel
import com.grupo4.smarthome.ui.screens.routines.RoutinesViewModelFactory
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

    val authFlowViewModel: AuthFlowViewModel = viewModel()
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

    LaunchedEffect(sessionState, currentBaseRoute) {
        if (sessionState is SessionState.Unauthenticated &&
            currentBaseRoute != null &&
            currentBaseRoute !in AuthScreen.routes
        ) {
            isDrawerVisible = false
            showAddHome = false
            navController.navigate(AuthScreen.Welcome.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
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

    var roomsFullscreenDialogOpen by remember { mutableStateOf(false) }
    val inRoomDetail = currentBaseRoute == Screen.Rooms.route && roomsUiState.selectedRoomId != null
    val showBottomBar = !isTablet &&
        !isDrawerVisible &&
        !inRoomDetail &&
        !roomsFullscreenDialogOpen &&
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
        onDeleteHome = { selectHomeViewModel.openDeleteDialog(it) },
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
            authGraph(
                navController = navController,
                isTablet = isTablet,
                hasSelectedHome = hasSelectedHome,
                authFlowViewModel = authFlowViewModel,
            )

            selectHomeGraph(
                navController = navController,
                windowSize = windowSize,
                selectHomeViewModel = selectHomeViewModel,
                onAddHome = { showAddHome = true },
                onSearchClick = openSearch,
            )

            mainGraph(
                navController = navController,
                windowSize = windowSize,
                isTablet = isTablet,
                homeViewModel = homeViewModel,
                roomsViewModel = roomsViewModel,
                routinesViewModel = routinesViewModel,
                searchViewModel = searchViewModel,
                notificationsViewModel = notificationsViewModel,
                actions = MainActions(
                    openSearch = openSearch,
                    openNotifications = openNotifications,
                    openDrawer = openDrawer,
                    onBack = onBack,
                    openQuickActions = openQuickActions,
                    openCreateRoom = openCreateRoom,
                    openCreateDevice = openCreateDevice,
                    openCreateRoutine = openCreateRoutine,
                    performLogout = performLogout,
                    onThemePreferenceChange = onThemePreferenceChange,
                    onRoomsFullscreenDialogChange = { roomsFullscreenDialogOpen = it },
                    onEditRoutine = { routineEditTargetId = it },
                    onAddRoutineAction = { routineActionTargetId = it },
                    onScannedPayload = { scannedPayload = it },
                ),
                unreadCount = { unreadNotificationCount },
                activeHomeName = { activeHomeName },
                activeHomeId = { selectHomeUiState.activeHomeId },
                themePreference = { themePreference },
                showQuickAddFab = {
                    !isTablet && !showQuickActions && !showCreateRoom &&
                        !showCreateDevice && !showCreateRoutine
                },
            )
            accountGraph(
                navController = navController,
                isTablet = isTablet,
            )
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
                        onDeleteHome = { home ->
                            selectHomeViewModel.openDeleteDialog(home)
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
