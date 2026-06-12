package com.grupo4.smarthome.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainShellScaffold(
    title: String?,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
    onNotificationsClick: (() -> Unit)? = null,
    notificationCount: Int = 0,
    onBackClick: (() -> Unit)? = null,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    val openDrawer = LocalOpenDrawer.current
    val background = MaterialTheme.colorScheme.background

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(background),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = background,
        topBar = {
            SmartHomeTopBar(
                title = title,
                onMenuClick = openDrawer,
                onSearchClick = onSearchClick,
                onProfileClick = onProfileClick,
                onNotificationsClick = onNotificationsClick,
                notificationCount = notificationCount,
                onBackClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(background)
                    .statusBarsPadding()
                    .padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 4.dp),
            )
        },
        floatingActionButton = floatingActionButton,
        content = content,
    )
}
