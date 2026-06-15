package com.grupo4.smarthome.ui.screens.routines

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.screens.dashboard.DashboardCallbacks
import com.grupo4.smarthome.ui.theme.RoutineDay
import com.grupo4.smarthome.ui.theme.RoutinePlay
import com.grupo4.smarthome.ui.theme.SmartHomeIcons
import com.grupo4.smarthome.ui.theme.StarActive

@Composable
fun TabletRoutinesDetailLayout(
    onBack: () -> Unit = {},
    onNavigateHome: () -> Unit = {},
    onNavigateRooms: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onAddRoutine: () -> Unit = {},
    onDelete: () -> Unit = {},
    initialRoutineId: String? = null,
    masterRoutines: List<RoutineUi> = sampleRoutines,
    routine: RoutineDetailUi = sampleRoutineDetail,
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<RoutineCategory?>(null) }
    var selectedRoutineId by remember(initialRoutineId, masterRoutines) {
        mutableStateOf(initialRoutineId ?: masterRoutines.firstOrNull()?.id)
    }
    val visibleMasterRoutines = remember(selectedCategory, masterRoutines) {
        if (selectedCategory == null) masterRoutines
        else masterRoutines.filter { it.category == selectedCategory }
    }

    LaunchedEffect(visibleMasterRoutines, selectedRoutineId) {
        if (selectedRoutineId == null || visibleMasterRoutines.none { it.id == selectedRoutineId }) {
            selectedRoutineId = visibleMasterRoutines.firstOrNull()?.id
        }
    }

    val selectedRoutine = visibleMasterRoutines.firstOrNull { it.id == selectedRoutineId }
        ?: masterRoutines.firstOrNull { it.id == selectedRoutineId }
        ?: masterRoutines.firstOrNull()
    val selectedDetail = selectedRoutine?.toDetailUi() ?: routine

    var enabled by remember(selectedRoutine?.id) { mutableStateOf(selectedDetail.enabled) }
    var name by remember(selectedRoutine?.id) { mutableStateOf(selectedDetail.name) }
    var time by remember(selectedRoutine?.id) { mutableStateOf(selectedDetail.time) }
    var days by remember(selectedRoutine?.id) { mutableStateOf(selectedDetail.days) }

    val daysSummary = daysSummaryText(days)

    val callbacks = DashboardCallbacks(
        onProfileClick = onProfileClick,
        onNavigateHome = onNavigateHome,
        onNavigateRooms = onNavigateRooms,
        onNavigateRoutines = {},
        onQuickAddClick = onAddRoutine,
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val compact = maxWidth < 720.dp
        val contentPadding = if (compact) 16.dp else 28.dp

        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            RoutinesTabletNavigationRail(callbacks = callbacks)
            Column(modifier = Modifier.fillMaxSize()) {
                RoutinesTabletTopBar(
                    onSearchClick = onSearchClick,
                    onHelpClick = {},
                    onProfileClick = onProfileClick,
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    MasterListPane(
                        modifier = Modifier
                            .weight(0.42f)
                            .fillMaxHeight(),
                        routines = masterRoutines,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it },
                        selectedRoutineId = selectedRoutineId,
                        onRoutineSelected = { selectedRoutineId = it },
                        contentPadding = contentPadding,
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.outlineVariant),
                    )
                    Column(
                        modifier = Modifier
                            .weight(0.58f)
                            .fillMaxHeight(),
                    ) {
                        DetailTopBar(
                            onBack = onBack,
                            onRename = { showRenameDialog = true },
                            onDelete = { showDeleteDialog = true },
                            showTitle = false,
                        )
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(
                                start = contentPadding,
                                end = contentPadding,
                                top = 8.dp,
                                bottom = 20.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            item {
                                HeroCard(
                                    routine = selectedDetail,
                                    name = name,
                                    time = time,
                                    daysSummary = daysSummary,
                                    enabled = enabled,
                                    onEnabledChange = { enabled = it },
                                )
                            }
                            item {
                                NavigationRowCard(
                                    icon = SmartHomeIcons.Schedule,
                                    title = stringResource(R.string.routine_detail_schedule_title),
                                    subtitle = time,
                                    onClick = { showScheduleDialog = true },
                                )
                            }
                            item {
                                DaysCard(
                                    days = days,
                                    onToggleDay = { index ->
                                        days = days.toMutableList().also { it[index] = !it[index] }
                                    },
                                )
                            }
                            item {
                                ActionsCard(
                                    actions = selectedDetail.actions,
                                    onAddAction = onAddRoutine,
                                )
                            }
                        }
                        DetailActionBar(
                            onDelete = { showDeleteDialog = true },
                            onSave = {},
                            contentPadding = contentPadding,
                        )
                    }
                }
            }
        }
    }

    if (showRenameDialog) {
        RenameRoutineDialog(
            currentName = name,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName ->
                name = newName
                showRenameDialog = false
            },
        )
    }
    if (showDeleteDialog) {
        DeleteRoutineDialog(
            routineName = name,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            },
        )
    }
    if (showScheduleDialog) {
        EditScheduleDialog(
            currentTime = time,
            onDismiss = { showScheduleDialog = false },
            onConfirm = { newTime ->
                time = newTime
                showScheduleDialog = false
            },
        )
    }
}

@Composable
private fun RoutinesTabletNavigationRail(callbacks: DashboardCallbacks) {
    Surface(
        modifier = Modifier
            .width(78.dp)
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "SH",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                RoutinesTabletRailItem(
                    icon = SmartHomeIcons.Home,
                    selected = false,
                    onClick = callbacks.onNavigateHome,
                )
                RoutinesTabletRailItem(
                    icon = SmartHomeIcons.Rooms,
                    selected = false,
                    onClick = callbacks.onNavigateRooms,
                )
                RoutinesTabletRailItem(
                    icon = SmartHomeIcons.Routines,
                    selected = true,
                    onClick = callbacks.onNavigateRoutines,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            RoutinesTabletRailItem(
                icon = SmartHomeIcons.Settings,
                selected = false,
                onClick = callbacks.onProfileClick,
            )
        }
    }
}

@Composable
private fun RoutinesTabletRailItem(
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun RoutinesTabletTopBar(
    onSearchClick: () -> Unit,
    onHelpClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 36.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = SmartHomeIcons.Search,
                    contentDescription = stringResource(R.string.dashboard_search_content_description),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onHelpClick) {
                Icon(
                    imageVector = SmartHomeIcons.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Surface(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onProfileClick),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "J",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun RoutinesSearchBar(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(999.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = SmartHomeIcons.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = stringResource(R.string.routines_search_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MasterListPane(
    modifier: Modifier,
    routines: List<RoutineUi>,
    selectedCategory: RoutineCategory?,
    onCategorySelected: (RoutineCategory?) -> Unit,
    selectedRoutineId: String?,
    onRoutineSelected: (String) -> Unit,
    contentPadding: Dp,
) {
    val visible = if (selectedCategory == null) routines
    else routines.filter { it.category == selectedCategory }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = contentPadding,
            end = contentPadding,
            top = 8.dp,
            bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column {
                Text(
                    text = stringResource(R.string.routines_header_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.routines_header_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            RoutinesSearchBar(modifier = Modifier.fillMaxWidth())
        }
        item {
            CategoryFilter(
                routines = routines,
                selectedCategory = selectedCategory,
                onCategorySelected = onCategorySelected,
            )
        }
        items(items = visible, key = { it.id }) { item ->
            MasterRoutineCard(
                routine = item,
                selected = item.id == selectedRoutineId,
                onClick = { onRoutineSelected(item.id) },
            )
        }
    }
}

@Composable
internal fun MasterRoutineCard(
    routine: RoutineUi,
    selected: Boolean,
    onClick: () -> Unit,
    onExecute: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
) {
    val border = if (selected) {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    }
    val background = if (selected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = background,
        border = border,
        shadowElevation = if (selected) 0.dp else 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LeadingIconBadge(icon = routine.category.icon)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = routine.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    MasterCategoryPill(category = routine.category)
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (routine.favorite) SmartHomeIcons.Favorite
                        else SmartHomeIcons.FavoriteBorder,
                        contentDescription = null,
                        tint = if (routine.favorite) StarActive
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = SmartHomeIcons.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp),
                    )
                    Text(
                        text = routine.time,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                )
                Text(
                    text = routine.actions.firstOrNull()?.let { action ->
                        routineActionCardLabel(action)
                    } ?: stringResource(R.string.routine_detail_no_actions_configured),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MasterDayLabels(days = routine.days, modifier = Modifier.weight(1f))
                MasterPlayButton(onClick = onExecute)
            }
        }
    }
}

@Composable
private fun MasterCategoryPill(category: RoutineCategory) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Text(
            text = stringResource(category.labelRes),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun MasterDayLabels(days: List<Boolean>, modifier: Modifier = Modifier) {
    val names = stringArrayResource(R.array.routines_day_names)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        days.forEachIndexed { index, active ->
            Text(
                text = names.getOrElse(index) { "" }.take(3),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                color = if (active) RoutineDay else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MasterPlayButton(onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .background(RoutinePlay),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = SmartHomeIcons.Play,
            contentDescription = stringResource(R.string.routines_play_content_description),
            tint = Color.White,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
internal fun DetailActionBar(
    onDelete: () -> Unit,
    onSave: () -> Unit,
    contentPadding: Dp,
    showDelete: Boolean = true,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = contentPadding, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showDelete) {
                Surface(
                    modifier = Modifier
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onDelete),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = SmartHomeIcons.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = stringResource(R.string.routine_detail_delete),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onSave),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary,
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = SmartHomeIcons.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = stringResource(R.string.routine_detail_save),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

