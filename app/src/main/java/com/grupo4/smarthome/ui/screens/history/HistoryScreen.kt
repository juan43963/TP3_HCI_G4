package com.grupo4.smarthome.ui.screens.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.window.core.layout.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.components.MainShellScaffold
import com.grupo4.smarthome.ui.components.TabletMainSidebar
import com.grupo4.smarthome.ui.components.TabletSidebarDestination
import com.grupo4.smarthome.ui.screens.dashboard.components.DashboardTopBar
import com.grupo4.smarthome.ui.screens.dashboard.layout.DashboardScaffold
import com.grupo4.smarthome.ui.theme.SmartHomeIcons
import com.grupo4.smarthome.ui.theme.SmartHomeTheme

enum class HistoryDeviceType(val icon: ImageVector) {
    VACUUM(SmartHomeIcons.Vacuum),
    BLINDS(SmartHomeIcons.Blinds),
    LIGHT(SmartHomeIcons.Light),
    AC(SmartHomeIcons.AirConditioner),
    SPEAKER(SmartHomeIcons.Speaker),
    DOOR(SmartHomeIcons.Door),
    FAUCET(SmartHomeIcons.Faucet),
    OVEN(SmartHomeIcons.Oven),
    ALARM(SmartHomeIcons.Alarm),
    FRIDGE(SmartHomeIcons.Fridge),
    OTHER(SmartHomeIcons.Devices),
}

data class HistoryEntryUi(
    val id: String,
    val action: String,
    val deviceName: String,
    val roomName: String,
    val deviceType: HistoryDeviceType,
    val timeLabel: String,
)

@Composable
fun HistoryScreen(
    windowSize: WindowSizeClass,
    onProfileClick: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onNavigateHome: () -> Unit = {},
    onNavigateRoutines: () -> Unit = {},
    onNavigateRooms: () -> Unit = {},
    onHomeSelectorClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onQuickAddClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    unreadNotificationCount: Int = 0,
    selectedHomeName: String = "Casa Principal",
    entries: List<HistoryEntryUi> = sampleHistory,
    isLoading: Boolean = false,
    isLoadingMore: Boolean = false,
    canLoadMore: Boolean = false,
    onLoadMore: () -> Unit = {},
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val isTablet = windowSize.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    val deviceNames = remember(entries) { entries.map { it.deviceName }.distinct() }
    var selectedDevice by remember { mutableStateOf<String?>(null) }
    val visibleEntries = remember(entries, selectedDevice) {
        if (selectedDevice == null) entries
        else entries.filter { it.deviceName == selectedDevice }
    }

    if (isTablet) {
        TabletHistoryLayout(
            visibleEntries = visibleEntries,
            deviceNames = deviceNames,
            selectedDevice = selectedDevice,
            onDeviceSelected = { selectedDevice = it },
            onRefresh = onRefresh,
            isLoading = isLoading,
            isLoadingMore = isLoadingMore,
            canLoadMore = canLoadMore,
            onLoadMore = onLoadMore,
            errorMessage = errorMessage,
            onRetry = onRetry,
            selectedHomeName = selectedHomeName,
            onNavigateHome = onNavigateHome,
            onNavigateRoutines = onNavigateRoutines,
            onNavigateRooms = onNavigateRooms,
            onHomeSelectorClick = onHomeSelectorClick,
            onSearchClick = onSearchClick,
            onProfileClick = onProfileClick,
            onQuickAddClick = onQuickAddClick,
            onNotificationsClick = onNotificationsClick,
            unreadNotificationCount = unreadNotificationCount,
            modifier = modifier,
        )
    } else {
        MainShellScaffold(
            title = null,
            onSearchClick = onSearchClick,
            onProfileClick = onProfileClick,
            onNotificationsClick = onNotificationsClick,
            notificationCount = unreadNotificationCount,
            modifier = modifier,
        ) { innerPadding ->
            HistoryFeed(
                visibleEntries = visibleEntries,
                deviceNames = deviceNames,
                selectedDevice = selectedDevice,
                onDeviceSelected = { selectedDevice = it },
                onRefresh = onRefresh,
                isLoading = isLoading,
                isLoadingMore = isLoadingMore,
                canLoadMore = canLoadMore,
                onLoadMore = onLoadMore,
                errorMessage = errorMessage,
                onRetry = onRetry,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 4.dp,
                    bottom = 32.dp,
                ),
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun TabletHistoryLayout(
    visibleEntries: List<HistoryEntryUi>,
    deviceNames: List<String>,
    selectedDevice: String?,
    onDeviceSelected: (String?) -> Unit,
    onRefresh: () -> Unit,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    canLoadMore: Boolean,
    onLoadMore: () -> Unit,
    errorMessage: String?,
    onRetry: () -> Unit,
    selectedHomeName: String,
    onNavigateHome: () -> Unit,
    onNavigateRoutines: () -> Unit,
    onNavigateRooms: () -> Unit,
    onHomeSelectorClick: () -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    onQuickAddClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    unreadNotificationCount: Int,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val portraitLike = maxWidth < 900.dp
        val contentPadding = if (portraitLike) 24.dp else 32.dp

        DashboardScaffold(
            sidebar = {
                TabletMainSidebar(
                    selectedDestination = TabletSidebarDestination.History,
                    compact = portraitLike,
                    onNavigateHome = onNavigateHome,
                    onNavigateRoutines = onNavigateRoutines,
                    onNavigateRooms = onNavigateRooms,
                    onNavigateHistory = {},
                    onAddClick = onQuickAddClick,
                )
            },
            topBar = {
                DashboardTopBar(
                    selectedHomeName = selectedHomeName,
                    onHomeSelectorClick = onHomeSelectorClick,
                    onSearchClick = onSearchClick,
                    onProfileClick = onProfileClick,
                    onNotificationsClick = onNotificationsClick,
                    notificationCount = unreadNotificationCount,
                    modifier = Modifier.padding(
                        start = contentPadding,
                        top = 24.dp,
                        end = contentPadding,
                        bottom = 14.dp,
                    ),
                )
            },
            content = {
                HistoryFeed(
                    visibleEntries = visibleEntries,
                    deviceNames = deviceNames,
                    selectedDevice = selectedDevice,
                    onDeviceSelected = onDeviceSelected,
                    onRefresh = onRefresh,
                    isLoading = isLoading,
                    isLoadingMore = isLoadingMore,
                    canLoadMore = canLoadMore,
                    onLoadMore = onLoadMore,
                    errorMessage = errorMessage,
                    onRetry = onRetry,
                    contentPadding = PaddingValues(
                        start = contentPadding,
                        end = contentPadding,
                        top = 8.dp,
                        bottom = 28.dp,
                    ),
                    itemModifier = Modifier.widthIn(max = 720.dp),
                )
            },
        )
    }
}

@Composable
private fun HistoryFeed(
    visibleEntries: List<HistoryEntryUi>,
    deviceNames: List<String>,
    selectedDevice: String?,
    onDeviceSelected: (String?) -> Unit,
    onRefresh: () -> Unit,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    canLoadMore: Boolean,
    onLoadMore: () -> Unit,
    errorMessage: String?,
    onRetry: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    itemModifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState, canLoadMore) {
        snapshotFlow {
            val layout = listState.layoutInfo
            (layout.visibleItemsInfo.lastOrNull()?.index ?: -1) to layout.totalItemsCount
        }
            .distinctUntilChanged()
            .collect { (lastVisible, total) ->
                if (canLoadMore && total > 0 && lastVisible >= total - 1) {
                    onLoadMore()
                }
            }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            HistoryHeader(
                onRefresh = onRefresh,
                modifier = itemModifier,
            )
        }
        item {
            DeviceFilter(
                deviceNames = deviceNames,
                selectedDevice = selectedDevice,
                onDeviceSelected = onDeviceSelected,
                modifier = itemModifier,
            )
        }
        item {
            when {
                isLoading && visibleEntries.isEmpty() -> LoadingHistory(modifier = itemModifier)
                errorMessage != null && visibleEntries.isEmpty() ->
                    ErrorHistory(message = errorMessage, onRetry = onRetry, modifier = itemModifier)
                visibleEntries.isEmpty() -> EmptyHistory(modifier = itemModifier)
                else -> HistoryList(entries = visibleEntries, modifier = itemModifier)
            }
        }
        if (isLoadingMore) {
            item {
                Box(
                    modifier = itemModifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryHeader(onRefresh: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.history_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.history_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = SmartHomeIcons.Refresh,
                    contentDescription = stringResource(R.string.history_refresh_content_description),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DeviceFilter(
    deviceNames: List<String>,
    selectedDevice: String?,
    onDeviceSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val label = selectedDevice ?: stringResource(R.string.history_filter_all)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.history_filter_label),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { expanded = true },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = SmartHomeIcons.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.history_filter_all)) },
                    onClick = {
                        onDeviceSelected(null)
                        expanded = false
                    },
                )
                deviceNames.forEach { name ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            onDeviceSelected(name)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryList(entries: List<HistoryEntryUi>, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Column {
            entries.forEachIndexed { index, entry ->
                HistoryRow(entry = entry)
                if (index != entries.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(entry: HistoryEntryUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = entry.deviceType.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.action,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(
                    R.string.history_device_metadata,
                    entry.deviceName,
                    entry.roomName,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = entry.timeLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LoadingHistory(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(
                text = stringResource(R.string.history_loading),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ErrorHistory(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onRetry) {
                Text(stringResource(R.string.history_retry))
            }
        }
    }
}

@Composable
private fun EmptyHistory(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = SmartHomeIcons.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp),
            )
            Text(
                text = stringResource(R.string.history_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.history_empty_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val sampleHistory = listOf(
    HistoryEntryUi("1", "Volviendo a la base", "Aspiradora", "Oficina", HistoryDeviceType.VACUUM, "hace 27 min"),
    HistoryEntryUi("2", "Pausado", "Aspiradora", "Oficina", HistoryDeviceType.VACUUM, "hace 27 min"),
    HistoryEntryUi("3", "Iniciado", "Aspiradora", "Oficina", HistoryDeviceType.VACUUM, "hace 27 min"),
    HistoryEntryUi("4", "Acción: down", "Persiana", "Cuarto principal", HistoryDeviceType.BLINDS, "hace 28 min"),
    HistoryEntryUi("5", "Apagado", "Lámpara", "Baño principal", HistoryDeviceType.LIGHT, "hace 28 min"),
    HistoryEntryUi("6", "Apagado", "Lámpara", "Corredor", HistoryDeviceType.LIGHT, "hace 28 min"),
    HistoryEntryUi("7", "Encendido", "Aire acond.", "Cuarto principal", HistoryDeviceType.AC, "hace 28 min"),
    HistoryEntryUi("8", "Acción: down", "Persiana", "Living", HistoryDeviceType.BLINDS, "hace 29 min"),
    HistoryEntryUi("9", "Reproduciendo", "Parlante", "Living", HistoryDeviceType.SPEAKER, "hace 30 min"),
    HistoryEntryUi("10", "Bloqueada", "Puerta", "Entrada", HistoryDeviceType.DOOR, "hace 31 min"),
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HistoryScreenLightPreview() {
    SmartHomeTheme(darkTheme = false) {
        HistoryListPreviewContent()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HistoryScreenDarkPreview() {
    SmartHomeTheme(darkTheme = true) {
        HistoryListPreviewContent()
    }
}

@Composable
private fun HistoryListPreviewContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HistoryHeader(onRefresh = {})
        DeviceFilter(
            deviceNames = sampleHistory.map { it.deviceName }.distinct(),
            selectedDevice = null,
            onDeviceSelected = {},
        )
        HistoryList(entries = sampleHistory)
    }
}
