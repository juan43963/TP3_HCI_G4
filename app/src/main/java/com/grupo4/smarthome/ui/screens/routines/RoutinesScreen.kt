package com.grupo4.smarthome.ui.screens.routines

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.components.MainShellScaffold

@Composable
fun RoutinesScreen(
    onProfileClick: () -> Unit = {},
) {
    MainShellScaffold(
        title = stringResource(R.string.nav_routines),
        onSearchClick = {},
        onProfileClick = onProfileClick,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.nav_routines),
                style = MaterialTheme.typography.headlineMedium,
            )
        }
    }
}
