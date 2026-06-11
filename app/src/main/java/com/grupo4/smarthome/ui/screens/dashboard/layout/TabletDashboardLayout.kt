package com.grupo4.smarthome.ui.screens.dashboard.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.components.LocalOpenDrawer
import com.grupo4.smarthome.ui.screens.dashboard.layout.phone.PhoneQuickAddFab
import com.grupo4.smarthome.ui.screens.dashboard.DashboardCallbacks
import com.grupo4.smarthome.ui.screens.dashboard.DashboardPresentationState
import com.grupo4.smarthome.ui.screens.dashboard.DashboardUiState
import com.grupo4.smarthome.ui.screens.dashboard.components.DashboardBodyContent
import com.grupo4.smarthome.ui.screens.dashboard.components.DashboardHeader
import com.grupo4.smarthome.ui.screens.dashboard.components.DashboardTopBar
import com.grupo4.smarthome.ui.screens.dashboard.components.WelcomeSummaryCard

@Composable
fun TabletDashboardLayout(
    state: DashboardUiState,
    presentation: DashboardPresentationState,
    callbacks: DashboardCallbacks,
    modifier: Modifier = Modifier,
) {
    val openDrawer = LocalOpenDrawer.current
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val portraitLike = maxWidth < 900.dp
        val contentPadding = if (portraitLike) 24.dp else 32.dp
        var quickAddMenuExpanded by remember { mutableStateOf(false) }

        DashboardScaffold(
            sidebar = {
                TabletSidebar(
                    callbacks = callbacks,
                    compact = portraitLike,
                    quickAddMenuExpanded = quickAddMenuExpanded,
                    onQuickAddMenuExpandedChange = { quickAddMenuExpanded = it },
                )
            },
            topBar = {
                DashboardTopBar(
                    selectedHomeName = state.selectedHomeName,
                    onHomeSelectorClick = openDrawer,
                    onSearchClick = callbacks.onSearchClick,
                    onProfileClick = callbacks.onProfileClick,
                    onNotificationsClick = callbacks.onNotificationsClick,
                    notificationCount = state.unreadNotificationCount,
                    modifier = Modifier.padding(
                        start = contentPadding,
                        top = 24.dp,
                        end = contentPadding,
                        bottom = 14.dp,
                    ),
                )
            },
            content = {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = contentPadding,
                        end = contentPadding,
                        top = 8.dp,
                        bottom = 28.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(22.dp),
                ) {
                    item {
                        Column {
                            DashboardHeader()
                            Spacer(modifier = Modifier.height(16.dp))
                            WelcomeSummaryCard(
                                userName = state.userName,
                                activeDevicesCount = state.activeDevicesCount,
                                roomsCount = state.roomsCount,
                            )
                        }
                    }
                    item {
                        DashboardBodyContent(
                            presentation = presentation,
                            callbacks = callbacks,
                        )
                    }
                }
            },
        )

        TabletQuickActionsOverlay(
            visible = quickAddMenuExpanded,
            compact = portraitLike,
            onAddRoom = callbacks.onCreateRoom,
            onAddDevice = callbacks.onCreateDevice,
            onAddRoutine = callbacks.onCreateRoutine,
            onDismiss = { quickAddMenuExpanded = false },
        )

        if (callbacks.showQuickAddFab) {
            PhoneQuickAddFab(
                onClick = callbacks.onQuickAddClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(16.dp),
                contentDescriptionRes = R.string.quick_add_fab_open,
            )
        }
    }
}
