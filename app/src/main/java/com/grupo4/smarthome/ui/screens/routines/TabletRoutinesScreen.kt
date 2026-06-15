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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.components.LocalOpenDrawer
import com.grupo4.smarthome.ui.components.SectionEditIconButton
import com.grupo4.smarthome.ui.components.TabletMainSidebar
import com.grupo4.smarthome.ui.components.TabletPanelSurface
import com.grupo4.smarthome.ui.components.TabletSidebarDestination
import com.grupo4.smarthome.ui.screens.dashboard.DashboardCallbacks
import com.grupo4.smarthome.ui.screens.dashboard.components.DashboardTopBar
import com.grupo4.smarthome.ui.screens.dashboard.layout.DashboardScaffold
import com.grupo4.smarthome.ui.screens.dashboard.layout.phone.PhoneQuickAddFab
import com.grupo4.smarthome.ui.util.isPhoneLandscape
import com.grupo4.smarthome.ui.theme.BrandIndigoDark
import com.grupo4.smarthome.ui.theme.SmartHomeIcons
import com.grupo4.smarthome.ui.theme.SmartHomeTheme

@Composable
fun TabletRoutinesScreen(
    routines: List<RoutineUi> = sampleRoutines,
    onProfileClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    unreadNotificationCount: Int = 0,
    onAddRoutine: () -> Unit = {},
    onCreateRoom: () -> Unit = {},
    onCreateDevice: () -> Unit = {},
    onNavigateHome: () -> Unit = {},
    onNavigateRooms: () -> Unit = {},
    onNavigateHistory: () -> Unit = {},
    onRoutineClick: (String) -> Unit = {},
    onExecuteRoutine: (String) -> Unit = {},
    onToggleRoutineFavorite: (String) -> Unit = {},
    onEditRoutine: (String) -> Unit = {},
    onSaveRoutine: (id: String, name: String, time24h: String, days: List<Boolean>, enabled: Boolean) -> Unit = { _, _, _, _, _ -> },
    onDeleteRoutine: (String) -> Unit = {},
    detailFor: (String) -> RoutineDetailUi? = { null },
    errorMessage: String? = null,
    infoMessage: String? = null,
    onDismissError: () -> Unit = {},
    onDismissInfo: () -> Unit = {},
    onQuickAddClick: () -> Unit = onAddRoutine,
    modifier: Modifier = Modifier,
) {
    val openDrawer = LocalOpenDrawer.current
    var selectedCategory by remember { mutableStateOf<RoutineCategory?>(null) }
    var selectedRoutineId by remember(routines) { mutableStateOf(routines.firstOrNull()?.id) }
    val visibleRoutines = remember(selectedCategory, routines) {
        if (selectedCategory == null) routines
        else routines.filter { it.category == selectedCategory }
    }
    val selectedRoutine = remember(selectedRoutineId, visibleRoutines) {
        visibleRoutines.firstOrNull { it.id == selectedRoutineId }
    }
    var openedRoutineId by remember(routines) { mutableStateOf<String?>(null) }
    val openedRoutine = remember(openedRoutineId, routines) {
        routines.firstOrNull { it.id == openedRoutineId }
    }
    var editMode by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<RoutineUi?>(null) }
    var deleteTarget by remember { mutableStateOf<RoutineUi?>(null) }

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
        onHistoryClick = onNavigateHistory,
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val phoneLandscape = isPhoneLandscape(maxWidth, maxHeight)
        val portraitLike = maxWidth < 900.dp
        val contentPadding = if (portraitLike) 24.dp else 32.dp

        if (phoneLandscape && openedRoutine != null) {
            BackHandler { openedRoutineId = null }
            RoutineDetailScreen(
                routine = detailFor(openedRoutine.id) ?: openedRoutine.toDetailUi(),
                onBack = { openedRoutineId = null },
                onDelete = {
                    openedRoutineId = null
                    onDeleteRoutine(openedRoutine.id)
                },
                onAddAction = onAddRoutine,
                onSave = { name, time24h, days, enabled ->
                    onSaveRoutine(openedRoutine.id, name, time24h, days, enabled)
                },
            )
            return@BoxWithConstraints
        }

        DashboardScaffold(
            sidebar = {
                TabletMainSidebar(
                    selectedDestination = TabletSidebarDestination.Routines,
                    compact = portraitLike || phoneLandscape,
                    onNavigateHome = callbacks.onNavigateHome,
                    onNavigateRoutines = callbacks.onNavigateRoutines,
                    onNavigateRooms = callbacks.onNavigateRooms,
                    onNavigateHistory = callbacks.onHistoryClick,
                )
            },
            topBar = {
                DashboardTopBar(
                    selectedHomeName = "",
                    onHomeSelectorClick = openDrawer,
                    onSearchClick = onSearchClick,
                    onProfileClick = onProfileClick,
                    onNotificationsClick = onNotificationsClick,
                    notificationCount = unreadNotificationCount,
                    useCompactHomeMenu = true,
                    modifier = Modifier.padding(
                        start = contentPadding,
                        top = 24.dp,
                        end = contentPadding,
                        bottom = 14.dp,
                    ),
                )
            },
            content = {
                if (phoneLandscape) {
                    TabletRoutinesGrid(
                        routines = routines,
                        visibleRoutines = visibleRoutines,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it },
                        portraitLike = true,
                        contentPadding = contentPadding,
                        onAddRoutine = onAddRoutine,
                        editMode = editMode,
                        onToggleEdit = { editMode = !editMode },
                        onRenameRoutine = { renameTarget = it },
                        onDeleteRoutineRequest = { deleteTarget = it },
                        onRoutineSelected = { routine ->
                            openedRoutineId = routine.id
                            onRoutineClick(routine.id)
                        },
                        onExecuteRoutine = onExecuteRoutine,
                        onToggleRoutineFavorite = onToggleRoutineFavorite,
                    )
                } else if (routines.isEmpty() && !portraitLike) {
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
                        editMode = editMode,
                        onToggleEdit = { editMode = !editMode },
                        onRenameRoutine = { renameTarget = it },
                        onDeleteRoutineRequest = { deleteTarget = it },
                        onRoutineSelected = { routine ->
                            selectedRoutineId = routine.id
                            onRoutineClick(routine.id)
                        },
                        onExecuteRoutine = onExecuteRoutine,
                        onToggleRoutineFavorite = onToggleRoutineFavorite,
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
                        editMode = editMode,
                        onToggleEdit = { editMode = !editMode },
                        onRenameRoutine = { renameTarget = it },
                        onDeleteRoutineRequest = { deleteTarget = it },
                        contentPadding = contentPadding,
                        forceSinglePane = phoneLandscape,
                        onExecuteRoutine = onExecuteRoutine,
                        onToggleRoutineFavorite = onToggleRoutineFavorite,
                        onEditRoutine = onEditRoutine,
                        onSaveRoutine = onSaveRoutine,
                        detailFor = detailFor,
                    )
                }
            },
        )

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

        renameTarget?.let { target ->
            RenameRoutineDialog(
                currentName = target.name,
                onDismiss = { renameTarget = null },
                onConfirm = { newName ->
                    val d = detailFor(target.id) ?: target.toDetailUi()
                    onSaveRoutine(target.id, newName, parseDisplayTimeTo24h(d.time), d.days, d.enabled)
                    renameTarget = null
                    editMode = false
                },
            )
        }
        deleteTarget?.let { target ->
            DeleteRoutineDialog(
                routineName = target.name,
                onDismiss = { deleteTarget = null },
                onConfirm = {
                    onDeleteRoutine(target.id)
                    deleteTarget = null
                    editMode = false
                },
            )
        }

        if (phoneLandscape) {
            PhoneQuickAddFab(
                onClick = onQuickAddClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(16.dp),
                contentDescriptionRes = R.string.quick_add_fab_open,
            )
        }
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
    editMode: Boolean,
    onToggleEdit: () -> Unit,
    onRenameRoutine: (RoutineUi) -> Unit,
    onDeleteRoutineRequest: (RoutineUi) -> Unit,
    onRoutineSelected: (RoutineUi) -> Unit,
    onExecuteRoutine: (String) -> Unit = {},
    onToggleRoutineFavorite: (String) -> Unit = {},
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
            TabletRoutinesHeader(
                actions = {
                    TabletRoutinesEditCreateActions(
                        editMode = editMode,
                        showEditButton = routines.isNotEmpty(),
                        onToggleEdit = onToggleEdit,
                        onAddRoutine = onAddRoutine,
                    )
                },
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            CategoryFilter(
                routines = routines,
                selectedCategory = selectedCategory,
                onCategorySelected = onCategorySelected,
                roomsStyle = true,
            )
        }
        if (routines.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                RoutinesEmptyStateCard(
                    onCreateRoutineClick = onAddRoutine,
                    dashed = true,
                    visual = RoutinesEmptyVisual.Home,
                )
            }
        } else {
            gridItems(items = visibleRoutines, key = { it.id }) { routine ->
                EditableRoutineCard(
                    editMode = editMode,
                    onEdit = { onRenameRoutine(routine) },
                    onDelete = { onDeleteRoutineRequest(routine) },
                ) {
                    RoutineCard(
                        routine = routine,
                        onClick = { onRoutineSelected(routine) },
                        onExecute = { onExecuteRoutine(routine.id) },
                        onToggleFavorite = { onToggleRoutineFavorite(routine.id) },
                    )
                }
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
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = contentPadding, end = contentPadding, bottom = 28.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        TabletPanelSurface(
            modifier = Modifier
                .weight(0.42f)
                .fillMaxHeight(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                TabletRoutinesHeaderActions(onAddRoutine = onAddRoutine)
                CategoryFilter(
                    routines = routines,
                    selectedCategory = selectedCategory,
                    onCategorySelected = onCategorySelected,
                    roomsStyle = true,
                )
                RoutinesEmptyStateCard(
                    onCreateRoutineClick = onAddRoutine,
                    dashed = true,
                    visual = RoutinesEmptyVisual.Home,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }
        }

        TabletPanelSurface(
            modifier = Modifier
                .weight(0.58f)
                .fillMaxHeight(),
        ) {
            RoutinesEmptyDetailPane()
        }
    }
}

@Composable
private fun TabletRoutinesEditCreateActions(
    editMode: Boolean,
    showEditButton: Boolean,
    onToggleEdit: () -> Unit,
    onAddRoutine: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showEditButton) {
            SectionEditIconButton(
                onClick = onToggleEdit,
                contentDescription = stringResource(
                    if (editMode) R.string.routines_finish_editing else R.string.routines_edit,
                ),
            )
        }
        Button(
            onClick = onAddRoutine,
            modifier = Modifier.height(40.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandIndigoDark,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            contentPadding = PaddingValues(horizontal = 12.dp),
        ) {
            Icon(
                imageVector = SmartHomeIcons.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.routines_create_short),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun TabletRoutinesHeaderActions(onAddRoutine: () -> Unit) {
    TabletRoutinesHeader(
        actions = {
            Button(
                onClick = onAddRoutine,
                modifier = Modifier.height(40.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandIndigoDark,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                contentPadding = PaddingValues(horizontal = 12.dp),
            ) {
                Icon(
                    imageVector = SmartHomeIcons.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.routines_create_short),
                    fontWeight = FontWeight.Bold,
                )
            }
        },
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun TabletRoutinesTwoPane(
    routines: List<RoutineUi>,
    visibleRoutines: List<RoutineUi>,
    selectedCategory: RoutineCategory?,
    selectedRoutine: RoutineUi,
    onCategorySelected: (RoutineCategory?) -> Unit,
    onRoutineSelected: (RoutineUi) -> Unit,
    onAddRoutine: () -> Unit,
    editMode: Boolean,
    onToggleEdit: () -> Unit,
    onRenameRoutine: (RoutineUi) -> Unit,
    onDeleteRoutineRequest: (RoutineUi) -> Unit,
    contentPadding: androidx.compose.ui.unit.Dp,
    forceSinglePane: Boolean = false,
    onExecuteRoutine: (String) -> Unit = {},
    onToggleRoutineFavorite: (String) -> Unit = {},
    onEditRoutine: (String) -> Unit = {},
    onSaveRoutine: (id: String, name: String, time24h: String, days: List<Boolean>, enabled: Boolean) -> Unit = { _, _, _, _, _ -> },
    detailFor: (String) -> RoutineDetailUi? = { null },
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    val scope = rememberCoroutineScope()
    BackHandler(navigator.canNavigateBack()) {
        scope.launch { navigator.navigateBack() }
    }

    val directive = navigator.scaffoldDirective.let { base ->
        if (forceSinglePane) base.copy(maxHorizontalPartitions = 1) else base
    }

    ListDetailPaneScaffold(
        directive = directive,
        value = navigator.scaffoldValue,
        modifier = Modifier
            .fillMaxSize()
            .padding(start = contentPadding, end = contentPadding, bottom = 28.dp),
        listPane = {
            AnimatedPane {
                TabletPanelSurface(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        item {
                            TabletRoutinesHeader(
                                actions = {
                                    TabletRoutinesEditCreateActions(
                                        editMode = editMode,
                                        showEditButton = routines.isNotEmpty(),
                                        onToggleEdit = onToggleEdit,
                                        onAddRoutine = onAddRoutine,
                                    )
                                },
                            )
                        }
                        item {
                            CategoryFilter(
                                routines = routines,
                                selectedCategory = selectedCategory,
                                onCategorySelected = onCategorySelected,
                                roomsStyle = true,
                            )
                        }
                        lazyItems(items = visibleRoutines, key = { it.id }) { routine ->
                            EditableRoutineCard(
                                editMode = editMode,
                                onEdit = { onRenameRoutine(routine) },
                                onDelete = { onDeleteRoutineRequest(routine) },
                            ) {
                                MasterRoutineCard(
                                    routine = routine,
                                    selected = routine.id == selectedRoutine.id,
                                    onClick = {
                                        onRoutineSelected(routine)
                                        scope.launch {
                                            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, routine.id)
                                        }
                                    },
                                    onExecute = { onExecuteRoutine(routine.id) },
                                    onToggleFavorite = { onToggleRoutineFavorite(routine.id) },
                                )
                            }
                        }
                    }
                }
            }
        },
        detailPane = {
            AnimatedPane {
                TabletPanelSurface(modifier = Modifier.fillMaxSize()) {
                    TabletRoutineDetailPane(
                        routine = selectedRoutine,
                        detail = detailFor(selectedRoutine.id),
                        onEditRoutine = { onEditRoutine(selectedRoutine.id) },
                        onEnabledChange = { enabled ->
                            val selectedDetail = detailFor(selectedRoutine.id)
                                ?: selectedRoutine.toDetailUi()
                            onSaveRoutine(
                                selectedRoutine.id,
                                selectedDetail.name,
                                parseDisplayTimeTo24h(selectedDetail.time),
                                selectedDetail.days,
                                enabled,
                            )
                        },
                        contentPadding = 20.dp,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        },
    )
}

@Composable
private fun TabletRoutinesHeader(
    modifier: Modifier = Modifier,
    actions: (@Composable () -> Unit)? = null,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val compact = maxWidth < 760.dp

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.routines_header_title),
                    modifier = Modifier.weight(1f),
                    style = if (compact) {
                        MaterialTheme.typography.titleLarge
                    } else {
                        MaterialTheme.typography.headlineMedium
                    },
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (actions != null) {
                    actions()
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.routines_header_subtitle),
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
    onEditRoutine: () -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    contentPadding: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    val detail = detail ?: remember(routine.id) { routine.toDetailUi() }
    val enabled = detail.enabled
    val name = detail.name
    val time = detail.time
    val days = detail.days
    val daysSummary = daysSummaryText(days)

    Column(modifier = modifier) {
        DetailTopBar(
            onBack = { onBack?.invoke() },
            onRename = {},
            onDelete = {},
            onEditRoutine = onEditRoutine,
            showTitle = false,
            showBackButton = onBack != null,
            showOverflowMenu = false,
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
                    onEnabledChange = onEnabledChange,
                )
            }
            item {
                NavigationRowCard(
                    icon = SmartHomeIcons.Schedule,
                    title = stringResource(R.string.routine_detail_schedule_title),
                    subtitle = time,
                    onClick = {},
                    editable = false,
                )
            }
            item {
                DaysCard(
                    days = days,
                    editable = false,
                )
            }
            item {
                ActionsCard(
                    actions = detail.actions,
                    onAddAction = {},
                    showAddActionButton = false,
                )
            }
        }
    }
}

