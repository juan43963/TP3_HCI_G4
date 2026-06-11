package com.grupo4.smarthome.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.window.core.layout.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.screens.dashboard.layout.TabletDashboardLayout
import com.grupo4.smarthome.ui.screens.dashboard.layout.phone.PhoneDashboardLayout

@Composable
fun DashboardContent(
    windowSize: WindowSizeClass,
    state: DashboardUiState,
    callbacks: DashboardCallbacks,
    modifier: Modifier = Modifier,
) {
    val presentation = remember(state) { state.toPresentationState() }

    if (!windowSize.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)) {
        PhoneDashboardLayout(
            state = state,
            presentation = presentation,
            callbacks = callbacks,
            modifier = modifier,
        )
    } else {
        TabletDashboardLayout(
            state = state,
            presentation = presentation,
            callbacks = callbacks,
            modifier = modifier,
        )
    }
}

@Composable
fun PhoneDashboardPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.dashboard_phone_placeholder),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
