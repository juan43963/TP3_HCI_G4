package com.grupo4.smarthome.ui.screens.dashboard.layout.phone

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.screens.dashboard.DashboardCallbacks
import com.grupo4.smarthome.ui.screens.dashboard.DashboardPresentationState
import com.grupo4.smarthome.ui.screens.dashboard.DashboardUiState
import com.grupo4.smarthome.ui.screens.dashboard.components.DashboardBodyContent
import com.grupo4.smarthome.ui.screens.dashboard.components.DashboardSearchBar
import com.grupo4.smarthome.ui.screens.dashboard.components.WelcomeSummaryCard
import com.grupo4.smarthome.ui.theme.SmartHomeIcons
import com.grupo4.smarthome.ui.theme.SurfaceAlt
import kotlinx.coroutines.launch

@Composable
fun PhoneDashboardLayout(
    state: DashboardUiState,
    presentation: DashboardPresentationState,
    callbacks: DashboardCallbacks,
    modifier: Modifier = Modifier,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val closeDrawer: () -> Unit = { scope.launch { drawerState.close() } }
    var isSearchExpanded by rememberSaveable { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            PhoneHomeDrawer(
                state = state,
                callbacks = callbacks,
                onClose = closeDrawer,
            )
        },
        modifier = modifier,
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceAlt),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = SurfaceAlt,
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceAlt)
                        .statusBarsPadding()
                        .animateContentSize(animationSpec = tween(durationMillis = 220))
                        .padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 4.dp),
                ) {
                    PhoneTopBar(
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onSearchClick = {
                            isSearchExpanded = true
                            callbacks.onSearchClick()
                        },
                        onProfileClick = callbacks.onProfileClick,
                    )
                    AnimatedVisibility(
                        visible = isSearchExpanded,
                        enter = fadeIn(animationSpec = tween(140)) +
                            expandVertically(
                                animationSpec = tween(220),
                                expandFrom = Alignment.Top,
                            ),
                        exit = fadeOut(animationSpec = tween(120)) +
                            shrinkVertically(
                                animationSpec = tween(180),
                                shrinkTowards = Alignment.Top,
                            ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, top = 6.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            DashboardSearchBar(
                                onClick = callbacks.onSearchClick,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(onClick = { isSearchExpanded = false }) {
                                Icon(
                                    imageVector = SmartHomeIcons.Close,
                                    contentDescription = stringResource(
                                        R.string.dashboard_search_close_content_description,
                                    ),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            },
            bottomBar = {
                PhoneBottomNavigation(
                    callbacks = callbacks,
                    modifier = Modifier.navigationBarsPadding(),
                )
            },
            floatingActionButton = {
                PhoneQuickAddFab(onClick = callbacks.onQuickAddClick)
            },
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceAlt)
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
}
