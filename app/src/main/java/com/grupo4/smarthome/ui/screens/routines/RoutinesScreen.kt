package com.grupo4.smarthome.ui.screens.routines

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.components.MainShellScaffold
import com.grupo4.smarthome.ui.screens.dashboard.layout.phone.PhoneQuickAddFab
import com.grupo4.smarthome.ui.screens.rooms.components.RoomsSelectedChipBorder
import com.grupo4.smarthome.ui.screens.rooms.components.RoomsSelectedChipContainer
import com.grupo4.smarthome.ui.theme.BrandIndigoDark
import com.grupo4.smarthome.ui.theme.RoutineDay
import com.grupo4.smarthome.ui.theme.RoutinePlay
import com.grupo4.smarthome.ui.theme.SmartHomeIcons
import com.grupo4.smarthome.ui.theme.SmartHomeTheme
import com.grupo4.smarthome.ui.theme.StarActive

private val DayActiveColor = RoutineDay
private val PlayColor = RoutinePlay
private val FilterSelectedColor = BrandIndigoDark

enum class RoutineCategory(val labelRes: Int, val icon: ImageVector) {
    MORNING(R.string.routines_category_morning, SmartHomeIcons.MorningRoutine),
    NIGHT(R.string.routines_category_night, SmartHomeIcons.NightRoutine),
    COMFORT(R.string.routines_category_comfort, SmartHomeIcons.ComfortRoutine),
    SECURITY(R.string.routines_category_security, SmartHomeIcons.SecurityRoutine),
    CUSTOM(R.string.routines_category_custom, SmartHomeIcons.CustomRoutine),
}

data class RoutineUi(
    val id: String,
    val name: String,
    val category: RoutineCategory,
    val time: String,
    val actions: List<String>,
    val extraActions: Int,
    val days: List<Boolean>,
    val favorite: Boolean,
)

@Composable
fun RoutinesScreen(
    onProfileClick: () -> Unit = {},
    onAddRoutine: () -> Unit = {},
    onCreateFirstRoutine: () -> Unit = onAddRoutine,
    onNotificationsClick: () -> Unit = {},
    unreadNotificationCount: Int = 0,
    onRoutineClick: (String) -> Unit = {},
    onCreateRoom: () -> Unit = {},
    onCreateDevice: () -> Unit = {},
    onQuickAddClick: () -> Unit = onAddRoutine,
    showQuickAddFab: Boolean = true,
    isTablet: Boolean = false,
    onNavigateHome: () -> Unit = {},
    onNavigateRooms: () -> Unit = {},
    onNavigateHistory: () -> Unit = {},
    routines: List<RoutineUi> = sampleRoutines,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    infoMessage: String? = null,
    onExecuteRoutine: (String) -> Unit = {},
    onSaveRoutine: (id: String, name: String, time24h: String, days: List<Boolean>, enabled: Boolean) -> Unit = { _, _, _, _, _ -> },
    onDeleteRoutine: (String) -> Unit = {},
    onRetry: () -> Unit = {},
    onDismissError: () -> Unit = {},
    onDismissInfo: () -> Unit = {},
    detailFor: (String) -> RoutineDetailUi? = { null },
) {
    if (isTablet) {
        TabletRoutinesScreen(
            routines = routines,
            onProfileClick = onProfileClick,
            onNotificationsClick = onNotificationsClick,
            unreadNotificationCount = unreadNotificationCount,
            onAddRoutine = onAddRoutine,
            onCreateRoom = onCreateRoom,
            onCreateDevice = onCreateDevice,
            onNavigateHome = onNavigateHome,
            onNavigateRooms = onNavigateRooms,
            onNavigateHistory = onNavigateHistory,
            onRoutineClick = onRoutineClick,
            onExecuteRoutine = onExecuteRoutine,
            onSaveRoutine = onSaveRoutine,
            onDeleteRoutine = onDeleteRoutine,
            detailFor = detailFor,
            errorMessage = errorMessage,
            infoMessage = infoMessage,
            onDismissError = onDismissError,
            onDismissInfo = onDismissInfo,
            onQuickAddClick = onQuickAddClick,
        )
        return
    }

    var selectedCategory by remember { mutableStateOf<RoutineCategory?>(null) }
    val visibleRoutines = remember(selectedCategory, routines) {
        if (selectedCategory == null) routines
        else routines.filter { it.category == selectedCategory }
    }

    MainShellScaffold(
        title = stringResource(R.string.nav_routines),
        onSearchClick = {},
        onProfileClick = onProfileClick,
        onNotificationsClick = onNotificationsClick,
        notificationCount = unreadNotificationCount,
        floatingActionButton = {
            if (showQuickAddFab) {
                PhoneQuickAddFab(
                    onClick = onAddRoutine,
                    modifier = Modifier.navigationBarsPadding(),
                    contentDescriptionRes = R.string.quick_add_fab_open,
                )
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                routines.isEmpty() && isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                routines.isEmpty() && errorMessage != null -> {
                    RoutinesErrorState(message = errorMessage, onRetry = onRetry)
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 4.dp,
                            bottom = 96.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        item {
                            Column {
                                Text(
                                    text = stringResource(R.string.routines_header_title),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = stringResource(R.string.routines_header_subtitle),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        item {
                            CategoryFilter(
                                routines = routines,
                                selectedCategory = selectedCategory,
                                onCategorySelected = { selectedCategory = it },
                                roomsStyle = true,
                            )
                        }
                        if (routines.isEmpty()) {
                            item {
                                RoutinesEmptyStateCard(
                                    onCreateRoutineClick = onCreateFirstRoutine,
                                    compact = true,
                                )
                            }
                        } else {
                            items(items = visibleRoutines, key = { it.id }) { routine ->
                                RoutineCard(
                                    routine = routine,
                                    onClick = { onRoutineClick(routine.id) },
                                    onExecute = { onExecuteRoutine(routine.id) },
                                )
                            }
                        }
                    }
                }
            }

            val snackbarHostState = remember { SnackbarHostState() }
            val executedTemplate = stringResource(R.string.routines_executed)
            LaunchedEffect(errorMessage) {
                if (errorMessage != null && routines.isNotEmpty()) {
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
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding(),
            )
        }
    }
}

@Composable
private fun RoutinesErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            modifier = Modifier.clip(RoundedCornerShape(999.dp)).clickable(onClick = onRetry),
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.primary,
        ) {
            Text(
                text = stringResource(R.string.routines_retry),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            )
        }
    }
}

@Preview(name = "Routines Phone - Sin rutinas", showBackground = true, showSystemUi = true)
@Composable
private fun RoutinesScreenEmptyPreview() {
    SmartHomeTheme(darkTheme = false) {
        RoutinesScreen(
            routines = emptyList(),
            showQuickAddFab = true,
        )
    }
}

@Preview(name = "Routines Tablet - Sin rutinas", showBackground = true, widthDp = 1280, heightDp = 800)
@Composable
private fun RoutinesScreenTabletEmptyPreview() {
    SmartHomeTheme(darkTheme = false) {
        RoutinesScreen(
            routines = emptyList(),
            isTablet = true,
        )
    }
}

@Composable
internal fun CategoryFilter(
    routines: List<RoutineUi>,
    selectedCategory: RoutineCategory?,
    onCategorySelected: (RoutineCategory?) -> Unit,
    roomsStyle: Boolean = false,
) {
    val presentCategories = remember(routines) {
        RoutineCategory.entries.filter { cat -> routines.any { it.category == cat } }
    }
    BoxWithConstraints {
        val compact = roomsStyle && maxWidth < 760.dp
        Column {
            Text(
                text = stringResource(R.string.routines_filter_label),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(
                    when {
                        !roomsStyle -> 10.dp
                        compact -> 8.dp
                        else -> 12.dp
                    },
                ),
            ) {
                FilterChip(
                    label = stringResource(R.string.routines_filter_all),
                    count = routines.size,
                    selected = selectedCategory == null,
                    onClick = { onCategorySelected(null) },
                    roomsStyle = roomsStyle,
                    compact = compact,
                )
                presentCategories.forEach { category ->
                    FilterChip(
                        label = stringResource(category.labelRes),
                        count = routines.count { it.category == category },
                        selected = selectedCategory == category,
                        onClick = { onCategorySelected(category) },
                        roomsStyle = roomsStyle,
                        compact = compact,
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    roomsStyle: Boolean = false,
    compact: Boolean = false,
) {
    if (roomsStyle) {
        RoomsStyleFilterChip(
            label = label,
            count = count,
            selected = selected,
            onClick = onClick,
            compact = compact,
        )
        return
    }

    val containerColor = if (selected) FilterSelectedColor else MaterialTheme.colorScheme.surface
    val contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(999.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
            )
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (selected) Color.White.copy(alpha = 0.25f)
                        else MaterialTheme.colorScheme.primaryContainer
                    )
                    .padding(horizontal = 7.dp, vertical = 1.dp),
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) Color.White else MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun RoomsStyleFilterChip(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    compact: Boolean = false,
) {
    val containerColor = if (selected) RoomsSelectedChipContainer else MaterialTheme.colorScheme.surface
    val contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = Modifier
            .height(if (compact) 36.dp else 44.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
        border = if (selected) {
            BorderStroke(1.dp, RoomsSelectedChipBorder)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        },
        shadowElevation = if (selected || compact) 0.dp else 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (compact) 12.dp else 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp),
        ) {
            Text(
                text = label,
                style = if (compact) {
                    MaterialTheme.typography.labelMedium
                } else {
                    MaterialTheme.typography.labelLarge
                },
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
            )
            Box(
                modifier = Modifier
                    .background(
                        color = if (selected) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = CircleShape,
                    )
                    .padding(horizontal = if (compact) 6.dp else 8.dp, vertical = 2.dp),
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                )
            }
        }
    }
}

@Composable
internal fun RoutineCard(routine: RoutineUi, onClick: () -> Unit = {}, onExecute: () -> Unit = {}) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = routine.category.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = routine.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    CategoryChip(category = routine.category)
                }
                Icon(
                    imageVector = if (routine.favorite) SmartHomeIcons.Favorite
                    else SmartHomeIcons.FavoriteBorder,
                    contentDescription = stringResource(R.string.routines_favorite_content_description),
                    tint = if (routine.favorite) StarActive else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = SmartHomeIcons.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = routine.time,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                routine.actions.forEach { action ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = SmartHomeIcons.Routines,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = action,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                if (routine.extraActions > 0) {
                    Text(
                        text = stringResource(R.string.routines_more_actions, routine.extraActions),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 24.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DaysRow(days = routine.days, modifier = Modifier.weight(1f))
                PlayButton(onClick = onExecute)
            }
        }
    }
}

@Composable
private fun CategoryChip(category: RoutineCategory) {
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
private fun DaysRow(days: List<Boolean>, modifier: Modifier = Modifier) {
    val initials = stringArrayResource(R.array.routines_day_initials)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        days.forEachIndexed { index, active ->
            DayCircle(
                letter = initials.getOrElse(index) { "" },
                active = active,
            )
        }
    }
}

@Composable
private fun DayCircle(letter: String, active: Boolean) {
    val baseModifier = Modifier
        .size(30.dp)
        .clip(CircleShape)
    val styledModifier = if (active) {
        baseModifier.background(DayActiveColor)
    } else {
        baseModifier
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
    }
    Box(modifier = styledModifier, contentAlignment = Alignment.Center) {
        Text(
            text = letter,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PlayButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(PlayColor),
    ) {
        Icon(
            imageVector = SmartHomeIcons.Play,
            contentDescription = stringResource(R.string.routines_play_content_description),
            tint = Color.White,
            modifier = Modifier.size(22.dp),
        )
    }
}

internal val sampleRoutines = listOf(
    RoutineUi(
        id = "night",
        name = "Buenas noches",
        category = RoutineCategory.NIGHT,
        time = "11:00 PM",
        actions = listOf("turnOff luces sala", "turnOff parlante sala"),
        extraActions = 2,
        days = listOf(true, true, true, true, true, false, false),
        favorite = true,
    ),
    RoutineUi(
        id = "morning",
        name = "Buenos días",
        category = RoutineCategory.MORNING,
        time = "07:00 AM",
        actions = listOf("turnOn luces cocina", "set persianas 80%"),
        extraActions = 2,
        days = listOf(true, true, true, true, true, false, false),
        favorite = true,
    ),
    RoutineUi(
        id = "relax",
        name = "Relax",
        category = RoutineCategory.COMFORT,
        time = "08:30 PM",
        actions = listOf("set luces cálidas", "turnOn música"),
        extraActions = 1,
        days = listOf(false, false, false, false, true, true, true),
        favorite = false,
    ),
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RoutinesScreenLightPreview() {
    SmartHomeTheme(darkTheme = false) {
        RoutinesScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RoutinesScreenDarkPreview() {
    SmartHomeTheme(darkTheme = true) {
        RoutinesScreen()
    }
}

@Preview(name = "Tablet landscape", showBackground = true, widthDp = 1280, heightDp = 800)
@Composable
private fun RoutinesScreenTabletPreview() {
    SmartHomeTheme(darkTheme = false) {
        RoutinesScreen(isTablet = true)
    }
}

@Preview(name = "Tablet portrait", showBackground = true, widthDp = 834, heightDp = 1194)
@Composable
private fun RoutinesScreenTabletPortraitPreview() {
    SmartHomeTheme(darkTheme = false) {
        RoutinesScreen(isTablet = true)
    }
}
