package com.grupo4.smarthome.ui.screens.dashboard.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.grupo4.smarthome.ui.theme.SurfaceAlt

@Composable
fun DashboardScaffold(
    sidebar: @Composable () -> Unit,
    topBar: @Composable () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceAlt),
    ) {
        sidebar()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceAlt),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                topBar()
            }
            content()
        }
    }
}
