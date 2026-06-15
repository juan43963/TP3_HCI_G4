package com.grupo4.smarthome.ui.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.get
import androidx.window.core.layout.WindowSizeClass
import com.grupo4.smarthome.R
import com.grupo4.smarthome.SmartHomeApplication
import com.grupo4.smarthome.data.model.QrDevicePayload
import com.grupo4.smarthome.ui.screens.device.DeviceDetailScreen
import com.grupo4.smarthome.ui.screens.history.HistoryScreen
import com.grupo4.smarthome.ui.screens.history.HistoryViewModel
import com.grupo4.smarthome.ui.screens.history.HistoryViewModelFactory
import com.grupo4.smarthome.ui.screens.history.toHistoryEntries
import com.grupo4.smarthome.ui.screens.home.HomeScreen
import com.grupo4.smarthome.ui.screens.home.HomeViewModel
import com.grupo4.smarthome.ui.screens.notifications.NotificationSettingsScreen
import com.grupo4.smarthome.ui.screens.notifications.NotificationsScreen
import com.grupo4.smarthome.ui.screens.notifications.NotificationsViewModel
import com.grupo4.smarthome.ui.screens.notifications.toNotificationUi
import com.grupo4.smarthome.ui.screens.profile.ProfileScreen
import com.grupo4.smarthome.ui.screens.profile.ProfileViewModel
import com.grupo4.smarthome.ui.screens.profile.ProfileViewModelFactory
import com.grupo4.smarthome.ui.screens.rooms.RoomsScreen
import com.grupo4.smarthome.ui.screens.rooms.RoomsViewModel
import com.grupo4.smarthome.ui.screens.routines.RoutineDetailScreen
import com.grupo4.smarthome.ui.screens.routines.RoutinesScreen
import com.grupo4.smarthome.ui.screens.routines.RoutinesViewModel
import com.grupo4.smarthome.ui.screens.routines.deviceNamesById
import com.grupo4.smarthome.ui.screens.routines.deviceTypesById
import com.grupo4.smarthome.ui.screens.routines.toDetailUi
import com.grupo4.smarthome.ui.screens.routines.toRoutineUi
import com.grupo4.smarthome.ui.screens.scan.ScanNavEvent
import com.grupo4.smarthome.ui.screens.scan.ScanScreen
import com.grupo4.smarthome.ui.screens.scan.ScanViewModel
import com.grupo4.smarthome.ui.screens.scan.ScanViewModelFactory
import com.grupo4.smarthome.ui.screens.search.SearchCallbacks
import com.grupo4.smarthome.ui.screens.search.SearchScreen
import com.grupo4.smarthome.ui.screens.search.SearchViewModel
import com.grupo4.smarthome.ui.theme.ThemePreference

class MainActions(
    val openSearch: () -> Unit,
    val openNotifications: () -> Unit,
    val openDrawer: () -> Unit,
    val onBack: () -> Unit,
    val openQuickActions: () -> Unit,
    val openCreateRoom: () -> Unit,
    val openCreateDevice: () -> Unit,
    val openCreateRoutine: () -> Unit,
    val performLogout: () -> Unit,
    val onThemePreferenceChange: (ThemePreference) -> Unit,
    val onRoomsFullscreenDialogChange: (Boolean) -> Unit,
    val onEditRoutine: (String) -> Unit,
    val onAddRoutineAction: (String) -> Unit,
    val onScannedPayload: (QrDevicePayload) -> Unit,
)

fun NavGraphBuilder.mainGraph(
    navController: NavHostController,
    windowSize: WindowSizeClass,
    isTablet: Boolean,
    homeViewModel: HomeViewModel,
    roomsViewModel: RoomsViewModel,
    routinesViewModel: RoutinesViewModel,
    searchViewModel: SearchViewModel,
    notificationsViewModel: NotificationsViewModel,
    actions: MainActions,
    unreadCount: () -> Int,
    activeHomeName: () -> String,
    activeHomeId: () -> String?,
    themePreference: () -> ThemePreference,
    showQuickAddFab: () -> Boolean,
) {
    composable(Screen.Home.route) {
        val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
        val routinesUiState by routinesViewModel.uiState.collectAsStateWithLifecycle()
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
                onSearchClick = actions.openSearch,
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
                onNavigateNotifications = actions.openNotifications,
                unreadNotificationCount = unreadCount(),
                onQuickActionsOpen = actions.openQuickActions,
                onCreateRoom = actions.openCreateRoom,
                onCreateDevice = actions.openCreateDevice,
                onCreateRoutine = actions.openCreateRoutine,
                showQuickAddFab = showQuickAddFab(),
            )
            SnackbarHost(
                hostState = dashboardSnackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
    composable(Screen.Search.route) {
        val searchUiState by searchViewModel.uiState.collectAsStateWithLifecycle()
        val roomsUiState by roomsViewModel.uiState.collectAsStateWithLifecycle()
        val routinesUiState by routinesViewModel.uiState.collectAsStateWithLifecycle()
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
            activeHomeId = activeHomeId(),
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
                actions.openSearch()
            },
            onNotificationsClick = actions.openNotifications,
            unreadNotificationCount = unreadCount(),
            onQuickActionsOpen = actions.openQuickActions,
            onFullscreenDialogChange = actions.onRoomsFullscreenDialogChange,
        )
    }
    composable(Screen.Routines.route) {
        val routinesUiState by routinesViewModel.uiState.collectAsStateWithLifecycle()
        RoutinesScreen(
            onNotificationsClick = actions.openNotifications,
            unreadNotificationCount = unreadCount(),
            onSearchClick = actions.openSearch,
            onProfileClick = {
                navController.navigate(Screen.Profile.route) {
                    launchSingleTop = true
                }
            },
            onCreateRoom = actions.openCreateRoom,
            onCreateDevice = actions.openCreateDevice,
            onAddRoutine = if (isTablet) actions.openCreateRoutine else actions.openQuickActions,
            onCreateFirstRoutine = actions.openCreateRoutine,
            onQuickAddClick = actions.openQuickActions,
            showQuickAddFab = showQuickAddFab(),
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
            onEditRoutine = actions.onEditRoutine,
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
        val routinesUiState by routinesViewModel.uiState.collectAsStateWithLifecycle()
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
                onAddAction = { actions.onAddRoutineAction(routineId) },
                onSave = { name, time24h, days, enabled ->
                    routinesViewModel.saveRoutine(routineId, name, time24h, days, enabled)
                    navController.popBackStack()
                },
            )
        }
    }
    composable(Screen.SCAN_QR_ROUTE) {
        val roomsUiState by roomsViewModel.uiState.collectAsStateWithLifecycle()
        val scanViewModel: ScanViewModel = viewModel(
            factory = remember { ScanViewModelFactory() },
        )
        val scanState by scanViewModel.uiState.collectAsStateWithLifecycle()
        val scanNavEvent by scanViewModel.navEvent.collectAsStateWithLifecycle()
        val homeDevices = remember(roomsUiState.rooms) {
            roomsUiState.rooms.flatMap { it.devices }
        }
        val devicesLoaded = roomsUiState.devicesLoaded
        LaunchedEffect(Unit) {
            if (!roomsUiState.devicesLoaded && !roomsUiState.isLoading) roomsViewModel.loadRooms()
            if (roomsUiState.availableDeviceTypes.isEmpty()) roomsViewModel.refreshDeviceTypes()
        }
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
                    actions.onScannedPayload(event.payload)
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
        val roomsUiState by roomsViewModel.uiState.collectAsStateWithLifecycle()
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
        val application = context.applicationContext as SmartHomeApplication
        val profileFactory = remember(application) {
            ProfileViewModelFactory(
                application.appContainer.profileRepository,
                application.appContainer.sessionManager,
            )
        }
        val profileViewModel: ProfileViewModel = viewModel(factory = profileFactory)
        val profileUiState by profileViewModel.uiState.collectAsStateWithLifecycle()
        val notificationPreferences by notificationsViewModel.preferences.collectAsStateWithLifecycle()
        val profileSavedMessage = stringResource(R.string.profile_saved)
        LaunchedEffect(profileUiState.nameSaved) {
            if (profileUiState.nameSaved) {
                profileViewModel.consumeNameSaved()
                Toast.makeText(
                    context,
                    profileSavedMessage,
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
            themePreference = themePreference(),
            onThemePreferenceChange = actions.onThemePreferenceChange,
            onChangePassword = {
                navController.navigate(Screen.CHANGE_PASSWORD_ROUTE)
            },
            onLogout = actions.performLogout,
            onOpenNotificationSettings = {
                navController.navigate(Screen.NotificationSettings.route) { launchSingleTop = true }
            },
            notificationPreferences = notificationPreferences,
            onNotificationPreferencesChange = notificationsViewModel::setPreferences,
            onNotificationsClick = actions.openNotifications,
            onSearchClick = actions.openSearch,
            unreadNotificationCount = unreadCount(),
            enteredFromHome = enteredFromHome,
            onBack = actions.onBack,
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
        val roomsUiState by roomsViewModel.uiState.collectAsStateWithLifecycle()
        val context = LocalContext.current
        val application = context.applicationContext as SmartHomeApplication
        val historyFactory = remember(application) {
            HistoryViewModelFactory(application.appContainer.devicesRepository)
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
            onBack = actions.onBack,
            selectedHomeName = activeHomeName(),
            onSearchClick = actions.openSearch,
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
            onNotificationsClick = actions.openNotifications,
            unreadNotificationCount = unreadCount(),
            onQuickAddClick = actions.openQuickActions,
        )
    }
    composable(Screen.Notifications.route) {
        val roomsUiState by roomsViewModel.uiState.collectAsStateWithLifecycle()
        val notificationsUiState by notificationsViewModel.uiState.collectAsStateWithLifecycle()
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
            onBack = actions.onBack,
            selectedHomeName = activeHomeName(),
            onHomeSelectorClick = actions.openDrawer,
            onSearchClick = actions.openSearch,
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
        val notificationPreferences by notificationsViewModel.preferences.collectAsStateWithLifecycle()
        NotificationSettingsScreen(
            windowSize = windowSize,
            preferences = notificationPreferences,
            onPreferencesChange = notificationsViewModel::setPreferences,
            onBack = actions.onBack,
            onSearchClick = actions.openSearch,
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
