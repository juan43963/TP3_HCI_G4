package com.grupo4.smarthome.ui.screens.dashboard.layout.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.components.LocalOpenDrawer
import com.grupo4.smarthome.ui.components.SmartHomeTopBar
import com.grupo4.smarthome.ui.screens.dashboard.DashboardCallbacks
import com.grupo4.smarthome.ui.screens.dashboard.DashboardPresentationState
import com.grupo4.smarthome.ui.screens.dashboard.DashboardUiState
import com.grupo4.smarthome.ui.screens.dashboard.components.DashboardBodyContent
import com.grupo4.smarthome.ui.screens.dashboard.components.WelcomeSummaryCard

@Composable
fun PhoneDashboardLayout(
    state: DashboardUiState,
    presentation: DashboardPresentationState,
    callbacks: DashboardCallbacks,
    modifier: Modifier = Modifier,
) {
    val openDrawer = LocalOpenDrawer.current

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
                    .padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 4.dp),
            ) {
                SmartHomeTopBar(
                    title = stringResource(R.string.dashboard_title),
                    onMenuClick = openDrawer,
                    onSearchClick = callbacks.onSearchClick,
                    onProfileClick = callbacks.onProfileClick,
                    onNotificationsClick = callbacks.onNotificationsClick,
                    notificationCount = state.unreadNotificationCount,
                )
            }
        },
        floatingActionButton = {
            if (callbacks.showQuickAddFab) {
                PhoneQuickAddFab(
                    onClick = callbacks.onQuickAddClick,
                    contentDescriptionRes = R.string.quick_add_fab_open,
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 10.dp,
                bottom = 28.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                WelcomeSummaryCard(
                    userName = state.userName,
                    activeDevicesCount = state.activeDevicesCount,
                    roomsCount = state.roomsCount,
                )
            }
            item {
                DashboardBodyContent(
                    presentation = presentation,
                    callbacks = callbacks,
                )
            }
        }
    }
}
