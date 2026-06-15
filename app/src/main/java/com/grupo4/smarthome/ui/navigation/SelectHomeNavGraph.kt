package com.grupo4.smarthome.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.window.core.layout.WindowSizeClass
import com.grupo4.smarthome.ui.screens.firsthome.CreateFirstHomeScreen
import com.grupo4.smarthome.ui.screens.selecthome.SelectHomeScreen
import com.grupo4.smarthome.ui.screens.selecthome.SelectHomeViewModel

fun NavGraphBuilder.selectHomeGraph(
    navController: NavHostController,
    windowSize: WindowSizeClass,
    selectHomeViewModel: SelectHomeViewModel,
    onAddHome: () -> Unit,
    onSearchClick: () -> Unit,
) {
    composable(Screen.CREATE_FIRST_HOME_ROUTE) {
        CreateFirstHomeScreen(
            windowSize = windowSize,
            onCreateHome = onAddHome,
        )
    }

    composable(Screen.SELECT_HOME_ROUTE) {
        val selectHomeUiState by selectHomeViewModel.uiState.collectAsStateWithLifecycle()
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
            onAddHome = onAddHome,
            onSearchClick = onSearchClick,
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
}
