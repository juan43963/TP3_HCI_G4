package com.grupo4.smarthome.ui.screens.selecthome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.window.core.layout.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.data.model.HomeType
import com.grupo4.smarthome.ui.components.MainShellScaffold
import com.grupo4.smarthome.ui.screens.dashboard.layout.phone.PhoneQuickAddFab
import com.grupo4.smarthome.ui.screens.selecthome.layout.TabletSelectHomeLayout
import com.grupo4.smarthome.ui.theme.SmartHomeIcons
import com.grupo4.smarthome.ui.theme.SmartHomeTheme

data class SelectableHome(
    val id: String,
    val name: String,
    val roomsCount: Int,
    val devicesCount: Int,
    val photoIndex: Int? = null,
    val type: HomeType = HomeType.OTRO,
    val ownerName: String? = null,
    val ownerEmail: String? = null,
)

@Composable
fun SelectHomeScreen(
    windowSize: WindowSizeClass,
    userName: String,
    homes: List<SelectableHome>,
    onHomeSelected: (SelectableHome) -> Unit,
    onAddHome: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
    onShareHome: (SelectableHome) -> Unit = {},
    onRenameHome: (SelectableHome) -> Unit = {},
    onDeleteHome: (SelectableHome) -> Unit = {},
) {
    if (!windowSize.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)) {
        PhoneSelectHomeLayout(
            userName = userName,
            homes = homes,
            onHomeSelected = onHomeSelected,
            onAddHome = onAddHome,
            onProfileClick = onProfileClick,
            isLoading = isLoading,
            errorMessage = errorMessage,
            onRetry = onRetry,
            onShareHome = onShareHome,
            onRenameHome = onRenameHome,
            onDeleteHome = onDeleteHome,
            modifier = modifier,
        )
    } else {
        TabletSelectHomeLayout(
            userName = userName,
            homes = homes,
            onHomeSelected = onHomeSelected,
            onAddHome = onAddHome,
            onProfileClick = onProfileClick,
            isLoading = isLoading,
            errorMessage = errorMessage,
            onRetry = onRetry,
            onShareHome = onShareHome,
            onRenameHome = onRenameHome,
            onDeleteHome = onDeleteHome,
            modifier = modifier,
        )
    }
}

@Composable
private fun PhoneSelectHomeLayout(
    userName: String,
    homes: List<SelectableHome>,
    onHomeSelected: (SelectableHome) -> Unit,
    onAddHome: () -> Unit,
    onProfileClick: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onShareHome: (SelectableHome) -> Unit = {},
    onRenameHome: (SelectableHome) -> Unit = {},
    onDeleteHome: (SelectableHome) -> Unit = {},
) {
    MainShellScaffold(
        title = null,
        onSearchClick = { /* search not implemented yet */ },
        onProfileClick = onProfileClick,
        floatingActionButton = {
            PhoneQuickAddFab(
                onClick = onAddHome,
                modifier = Modifier.navigationBarsPadding(),
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                Column {
                    Text(
                        text = stringResource(R.string.select_home_welcome_title, userName),
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
                isLoading && homes.isEmpty() -> item { SelectHomeLoading() }
                errorMessage != null && homes.isEmpty() -> item {
                    SelectHomeError(message = errorMessage, onRetry = onRetry)
                }
                homes.isEmpty() -> item { SelectHomeEmpty() }
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
    }
}

@Composable
internal fun SelectHomeLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.CircularProgressIndicator()
    }
}

@Composable
internal fun SelectHomeEmpty(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 48.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = SmartHomeIcons.Home,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.select_home_empty_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.select_home_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun SelectHomeError(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 48.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        androidx.compose.material3.OutlinedButton(onClick = onRetry) {
            Text(text = stringResource(R.string.select_home_retry))
        }
    }
}

@Composable
internal fun HomeCard(
    home: SelectableHome,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onShare: () -> Unit = {},
    onRename: () -> Unit = {},
    onDelete: () -> Unit = {},
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Column {
            val photoRes = homePhotoRes(home.photoIndex)
            if (photoRes != null) {
                Image(
                    painter = painterResource(photoRes),
                    contentDescription = home.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                )
            } else {
                HomePhotoPlaceholder(homeName = home.name)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = home.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Box {
                    var menuExpanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(
                                R.string.select_home_card_menu_content_description
                            ),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.select_home_menu_share)) },
                            leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onShare()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.select_home_menu_rename)) },
                            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onRename()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.select_home_menu_delete)) },
                            leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            },
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MetricRow(
                    icon = SmartHomeIcons.Rooms,
                    text = stringResource(R.string.select_home_rooms_count, home.roomsCount),
                )
                MetricRow(
                    icon = SmartHomeIcons.Devices,
                    text = stringResource(R.string.select_home_devices_count, home.devicesCount),
                )
            }
        }
    }
}

@Composable
private fun HomePhotoPlaceholder(homeName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.select_home_photo_placeholder, homeName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.select_home_browse_files),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = TextDecoration.Underline,
            )
        }
    }
}

@Composable
private fun MetricRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SelectHomeScreenPhonePreview() {
    SmartHomeTheme(darkTheme = false) {
        PhoneSelectHomeLayout(
            userName = "Juan",
            homes = listOf(
                SelectableHome("1", "Departamento", roomsCount = 8, devicesCount = 22),
                SelectableHome("2", "Oficina", roomsCount = 0, devicesCount = 0),
            ),
            onHomeSelected = {},
            onAddHome = {},
            onProfileClick = {},
            isLoading = false,
            errorMessage = null,
            onRetry = {},
        )
    }
}
