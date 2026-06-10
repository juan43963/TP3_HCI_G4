package com.grupo4.smarthome.ui.screens.routines

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.components.LocalOpenDrawer
import com.grupo4.smarthome.ui.screens.dashboard.DashboardCallbacks
import com.grupo4.smarthome.ui.screens.dashboard.components.DashboardTopBar
import com.grupo4.smarthome.ui.screens.dashboard.layout.DashboardScaffold
import com.grupo4.smarthome.ui.screens.dashboard.layout.SidebarDestination
import com.grupo4.smarthome.ui.screens.dashboard.layout.TabletSidebar
import com.grupo4.smarthome.ui.screens.dashboard.layout.TabletQuickActionsOverlay
import com.grupo4.smarthome.ui.theme.BrandIndigoDark
import com.grupo4.smarthome.ui.theme.SmartHomeIcons
import com.grupo4.smarthome.ui.theme.SmartHomeTheme

@Composable
fun TabletRoutinesScreen(
    routines: List<RoutineUi> = sampleRoutines,
    onProfileClick: () -> Unit = {},
    onAddRoutine: () -> Unit = {},
    onCreateRoom: () -> Unit = {},
    onCreateDevice: () -> Unit = {},
    onNavigateHome: () -> Unit = {},
    onNavigateRooms: () -> Unit = {},
    onRoutineClick: (String) -> Unit = {},
    onExecuteRoutine: (String) -> Unit = {},
    onSaveRoutine: (id: String, name: String, time24h: String, days: List<Boolean>, enabled: Boolean) -> Unit = { _, _, _, _, _ -> },
    onDeleteRoutine: (String) -> Unit = {},
    detailFor: (String) -> RoutineDetailUi? = { null },
    errorMessage: String? = null,
    infoMessage: String? = null,
    onDismissError: () -> Unit = {},
    onDismissInfo: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val openDrawer = LocalOpenDrawer.current
    var selectedCategory by remember { mutableStateOf<RoutineCategory?>(null) }
    var selectedRoutineId by remember(routines) { mutableStateOf(routines.firstOrNull()?.id) }
    var quickAddMenuExpanded by remember { mutableStateOf(false) }
    val visibleRoutines = remember(selectedCategory, routines) {
        if (selectedCategory == null) routines
        else routines.filter { it.category == selectedCategory }
    }
    val selectedRoutine = remember(selectedRoutineId, visibleRoutines) {
        visibleRoutines.firstOrNull { it.id == selectedRoutineId }
    }

    LaunchedEffect(visibleRoutines, selectedRoutineId) {
        if (visibleRoutines.isEmpty()) {
            selectedRoutineId = null
        } else if (selectedRoutineId == null || visibleRoutines.none { it.id == selectedRoutineId }) {
            selectedRoutineId = visibleRoutines.first().id
        }
    }

    val callbacks = DashboardCallbacks(
        onProfileClick = onProfileClick,
        onNavigateHome = onNavigateHome,
        onNavigateRooms = onNavigateRooms,
        onNavigateRoutines = {},
        onCreateRoom = onCreateRoom,
        onCreateDevice = onCreateDevice,
        onCreateRoutine = onAddRoutine,
        onQuickAddClick = onAddRoutine,
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val portraitLike = maxWidth < 900.dp
        val contentPadding = if (portraitLike) 24.dp else 32.dp

        DashboardScaffold(
            sidebar = {
                TabletSidebar(
                    callbacks = callbacks,
                    compact = portraitLike,
                    selected = SidebarDestination.Routines,
                    quickAddMenuExpanded = quickAddMenuExpanded,
                    onQuickAddMenuExpandedChange = { quickAddMenuExpanded = it },
                )
            },
            topBar = {
                DashboardTopBar(
                    selectedHomeName = stringResource(R.string.dashboard_home_main),
                    onHomeSelectorClick = openDrawer,
                    onSearchClick = {},
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
                if (routines.isEmpty() && !portraitLike) {
                    TabletRoutinesEmptyTwoPane(
                        routines = routines,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it },
                        onAddRoutine = onAddRoutine,
                        contentPadding = contentPadding,
                    )
                } else if (selectedRoutine == null) {
                    TabletRoutinesGrid(
                        routines = routines,
                        visibleRoutines = visibleRoutines,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it },
                        portraitLike = portraitLike,
                        contentPadding = contentPadding,
                        onAddRoutine = onAddRoutine,
                        onRoutineSelected = { routine ->
                            selectedRoutineId = routine.id
                            onRoutineClick(routine.id)
                        },
                        onExecuteRoutine = onExecuteRoutine,
                    )
                } else {
                    TabletRoutinesTwoPane(
                        routines = routines,
                        visibleRoutines = visibleRoutines,
                        selectedCategory = selectedCategory,
                        selectedRoutine = selectedRoutine,
                        onCategorySelected = { selectedCategory = it },
                        onRoutineSelected = { selectedRoutineId = it.id },
                        onAddRoutine = onAddRoutine,
                        contentPadding = contentPadding,
                        onExecuteRoutine = onExecuteRoutine,
                        onSaveRoutine = onSaveRoutine,
                        onDeleteRoutine = onDeleteRoutine,
                        detailFor = detailFor,
                    )
                }
            },
        )

        TabletQuickActionsOverlay(
            visible = quickAddMenuExpanded,
            compact = portraitLike,
            callbacks = callbacks,
            onDismiss = { quickAddMenuExpanded = false },
        )

        // Feedback no bloqueante (ejecución / errores)
        val snackbarHostState = remember { SnackbarHostState() }
        val executedTemplate = stringResource(R.string.routines_executed)
        LaunchedEffect(errorMessage) {
            if (errorMessage != null) {
                snackbarHostState.showSnackbar(errorMessage)
                onDismissError()
            }
        }
        LaunchedEffect(infoMessage) {
            if (infoMessage != null) {
                snackbarHostState.showSnackbar(executedTemplate.format(infoMessage))
                onDismissInfo()
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun TabletRoutinesGrid(
    routines: List<RoutineUi>,
    visibleRoutines: List<RoutineUi>,
    selectedCategory: RoutineCategory?,
    onCategorySelected: (RoutineCategory?) -> Unit,
    portraitLike: Boolean,
    contentPadding: androidx.compose.ui.unit.Dp,
    onAddRoutine: () -> Unit,
    onRoutineSelected: (RoutineUi) -> Unit,
    onExecuteRoutine: (String) -> Unit = {},
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(if (portraitLike) 1 else 2),
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
            TabletRoutinesHeader()
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            CategoryFilter(
                routines = routines,
                selectedCategory = selectedCategory,
                onCategorySelected = onCategorySelected,
            )
        }
        if (routines.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                RoutinesEmptyStateCard(
                    onCreateRoutineClick = onAddRoutine,
                    dashed = true,
                    visual = RoutinesEmptyVisual.Search,
                )
            }
        } else {
            gridItems(items = visibleRoutines, key = { it.id }) { routine ->
                RoutineCard(
                    routine = routine,
                    onClick = { onRoutineSelected(routine) },
                    onExecute = { onExecuteRoutine(routine.id) },
                )
            }
        }
    }
}

@Composable
private fun TabletRoutinesEmptyTwoPane(
    routines: List<RoutineUi>,
    selectedCategory: RoutineCategory?,
    onCategorySelected: (RoutineCategory?) -> Unit,
    onAddRoutine: () -> Unit,
    contentPadding: androidx.compose.ui.unit.Dp,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(0.42f)
                .fillMaxHeight(),
            contentPadding = PaddingValues(
                start = contentPadding,
                end = contentPadding,
                top = 8.dp,
                bottom = 28.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                TabletRoutinesHeaderActions(onAddRoutine = onAddRoutine)
            }
            item {
                RoutinesTabletSearchBar(modifier = Modifier.fillMaxWidth())
            }
            item {
                CategoryFilter(
                    routines = routines,
                    selectedCategory = selectedCategory,
                    onCategorySelected = onCategorySelected,
                )
            }
            item {
                RoutinesEmptyStateCard(
                    onCreateRoutineClick = onAddRoutine,
                    dashed = true,
                    visual = RoutinesEmptyVisual.Search,
                )
            }
        }

        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outlineVariant),
        )

        Box(
            modifier = Modifier
                .weight(0.58f)
                .fillMaxHeight()
                .padding(
                    start = contentPadding,
                    top = 8.dp,
                    end = contentPadding,
                    bottom = 28.dp,
                ),
        ) {
            RoutinesEmptyDetailPane()
        }
    }
}

@Composable
private fun TabletRoutinesHeaderActions(onAddRoutine: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TabletRoutinesHeader(modifier = Modifier.weight(1f))
        IconButton(
            onClick = {},
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
        ) {
            Icon(
                imageVector = SmartHomeIcons.Rename,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
        Button(
            onClick = onAddRoutine,
            modifier = Modifier.height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandIndigoDark,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            Icon(
                imageVector = SmartHomeIcons.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.dashboard_create_routine),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun TabletRoutinesTwoPane(
    routines: List<RoutineUi>,
    visibleRoutines: List<RoutineUi>,
    selectedCategory: RoutineCategory?,
    selectedRoutine: RoutineUi,
    onCategorySelected: (RoutineCategory?) -> Unit,
    onRoutineSelected: (RoutineUi) -> Unit,
    onAddRoutine: () -> Unit,
    contentPadding: androidx.compose.ui.unit.Dp,
    onExecuteRoutine: (String) -> Unit = {},
    onSaveRoutine: (id: String, name: String, time24h: String, days: List<Boolean>, enabled: Boolean) -> Unit = { _, _, _, _, _ -> },
    onDeleteRoutine: (String) -> Unit = {},
    detailFor: (String) -> RoutineDetailUi? = { null },
) {
    Row(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(0.42f)
                .fillMaxHeight(),
            contentPadding = PaddingValues(
                start = contentPadding,
                end = contentPadding,
                top = 8.dp,
                bottom = 28.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                TabletRoutinesHeader()
            }
            item {
                RoutinesTabletSearchBar(modifier = Modifier.fillMaxWidth())
            }
            item {
                CategoryFilter(
                    routines = routines,
                    selectedCategory = selectedCategory,
                    onCategorySelected = onCategorySelected,
                )
            }
            lazyItems(items = visibleRoutines, key = { it.id }) { routine ->
                MasterRoutineCard(
                    routine = routine,
                    selected = routine.id == selectedRoutine.id,
                    onClick = { onRoutineSelected(routine) },
                    onExecute = { onExecuteRoutine(routine.id) },
                )
            }
        }

        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outlineVariant),
        )

        TabletRoutineDetailPane(
            routine = selectedRoutine,
            detail = detailFor(selectedRoutine.id),
            onAddAction = onAddRoutine,
            onSaveRoutine = onSaveRoutine,
            onDeleteRoutine = { onDeleteRoutine(selectedRoutine.id) },
            contentPadding = contentPadding,
            modifier = Modifier
                .weight(0.58f)
                .fillMaxHeight(),
        )
    }
}

@Composable
private fun TabletRoutinesHeader(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
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

@Composable
private fun RoutinesTabletSearchBar(
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
        shadowElevation = 1.dp,
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
private fun TabletRoutineDetailPane(
    routine: RoutineUi,
    detail: RoutineDetailUi?,
    onAddAction: () -> Unit,
    onSaveRoutine: (id: String, name: String, time24h: String, days: List<Boolean>, enabled: Boolean) -> Unit,
    onDeleteRoutine: () -> Unit,
    contentPadding: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    // Preferimos el detalle derivado del dominio (acciones/categoría reales); si falta, caemos al de RoutineUi
    val detail = detail ?: remember(routine.id) { routine.toDetailUi() }
    var enabled by remember(routine.id) { mutableStateOf(detail.enabled) }
    var name by remember(routine.id) { mutableStateOf(detail.name) }
    var time by remember(routine.id) { mutableStateOf(detail.time) }
    var days by remember(routine.id) { mutableStateOf(detail.days) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showDaysDialog by remember { mutableStateOf(false) }
    val daysSummary = daysSummaryText(days)

    Column(modifier = modifier) {
        DetailTopBar(
            onBack = {},
            onRename = { showRenameDialog = true },
            onDelete = { showDeleteDialog = true },
            showTitle = false,
            showBackButton = false,
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
                    routine = detail,
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
                    onClick = { showDaysDialog = true },
                )
            }
            item {
                ActionsCard(
                    actions = detail.actions,
                    onAddAction = onAddAction,
                )
            }
        }

        DetailActionBar(
            onDelete = { showDeleteDialog = true },
            onSave = { onSaveRoutine(detail.id, name, parseDisplayTimeTo24h(time), days, enabled) },
            contentPadding = contentPadding,
            showDelete = false,
        )
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
                onDeleteRoutine()
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
    if (showDaysDialog) {
        EditDaysDialog(
            currentDays = days,
            onDismiss = { showDaysDialog = false },
            onConfirm = { newDays ->
                days = newDays
                showDaysDialog = false
            },
        )
    }
}

@Preview(name = "Tablet routines", showBackground = true, widthDp = 1280, heightDp = 800)
@Composable
private fun TabletRoutinesScreenPreview() {
    SmartHomeTheme(darkTheme = false) {
        TabletRoutinesScreen()
    }
}
