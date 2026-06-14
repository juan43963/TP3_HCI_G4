package com.grupo4.smarthome.ui.screens.selecthome.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.screens.dashboard.layout.DashboardScaffold
import com.grupo4.smarthome.ui.screens.selecthome.HomeCard
import com.grupo4.smarthome.ui.screens.selecthome.SelectHomeEmpty
import com.grupo4.smarthome.ui.screens.selecthome.SelectHomeError
import com.grupo4.smarthome.ui.screens.selecthome.SelectHomeLoading
import com.grupo4.smarthome.ui.screens.selecthome.SelectableHome
import com.grupo4.smarthome.ui.theme.SmartHomeTheme

@Composable
fun TabletSelectHomeLayout(
    userName: String,
    homes: List<SelectableHome>,
    onHomeSelected: (SelectableHome) -> Unit,
    onAddHome: () -> Unit,
    onProfileClick: () -> Unit,
    isLoading: Boolean = false,
    homesLoaded: Boolean = false,
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier,
    onShareHome: (SelectableHome) -> Unit = {},
    onRenameHome: (SelectableHome) -> Unit = {},
    onDeleteHome: (SelectableHome) -> Unit = {},
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val portraitLike = maxWidth < 900.dp
        val columns = if (portraitLike) 2 else 3
        val contentPadding = if (portraitLike) 24.dp else 32.dp

        DashboardScaffold(
            sidebar = {
                SelectHomeSidebar(
                    compact = portraitLike,
                    onSettingsClick = onProfileClick,
                    onAddHome = onAddHome,
                )
            },
            topBar = {
                SelectHomeTopBar(
                    onProfileClick = onProfileClick,
                    modifier = Modifier.padding(
                        start = contentPadding,
                        top = 24.dp,
                        end = contentPadding,
                        bottom = 14.dp,
                    ),
                )
            },
            content = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = contentPadding,
                        end = contentPadding,
                        top = 8.dp,
                        bottom = 28.dp,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column {
                            Text(
                                text = stringResource(
                                    R.string.select_home_welcome_title_inline,
                                    userName,
                                ),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.select_home_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    when {
                        !homesLoaded -> item(span = { GridItemSpan(maxLineSpan) }) {
                            SelectHomeLoading()
                        }
                        errorMessage != null && homes.isEmpty() -> item(span = { GridItemSpan(maxLineSpan) }) {
                            SelectHomeError(message = errorMessage, onRetry = onRetry)
                        }
                        homes.isEmpty() -> item(span = { GridItemSpan(maxLineSpan) }) {
                            SelectHomeEmpty()
                        }
                        else -> items(items = homes, key = { it.id }) { home ->
                            HomeCard(
                                home = home,
                                onClick = { onHomeSelected(home) },
                                onShare = { onShareHome(home) },
                                onRename = { onRenameHome(home) },
                                onDelete = { onDeleteHome(home) },
                            )
                        }
                    }
                }
            },
        )
    }
}

private val previewHomes = listOf(
    SelectableHome("apartment", "Departamento", roomsCount = 8, devicesCount = 22),
    SelectableHome("office", "Oficina", roomsCount = 0, devicesCount = 0),
    SelectableHome("beach", "Casa de playa", roomsCount = 5, devicesCount = 12),
)

@Preview(
    name = "Tablet landscape",
    showBackground = true,
    widthDp = 1280,
    heightDp = 800,
)
@Composable
private fun TabletSelectHomeLandscapePreview() {
    SmartHomeTheme(darkTheme = false) {
        TabletSelectHomeLayout(
            userName = "Juan Perez",
            homes = previewHomes,
            onHomeSelected = {},
            onAddHome = {},
            onProfileClick = {},
        )
    }
}

@Preview(
    name = "Tablet portrait",
    showBackground = true,
    widthDp = 800,
    heightDp = 1280,
)
@Composable
private fun TabletSelectHomePortraitPreview() {
    SmartHomeTheme(darkTheme = true) {
        TabletSelectHomeLayout(
            userName = "Juan Perez",
            homes = previewHomes,
            onHomeSelected = {},
            onAddHome = {},
            onProfileClick = {},
        )
    }
}
