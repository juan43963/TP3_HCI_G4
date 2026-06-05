package com.grupo4.smarthome.ui.screens.routines

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.grupo4.smarthome.ui.theme.BrandIndigoDark
import com.grupo4.smarthome.ui.theme.RoutineDay
import com.grupo4.smarthome.ui.theme.RoutinePlay
import com.grupo4.smarthome.ui.theme.SmartHomeIcons
import com.grupo4.smarthome.ui.theme.SmartHomeTheme
import com.grupo4.smarthome.ui.theme.StarActive

/** Active day chips — vivid violet, distinct from the play button. */
private val DayActiveColor = RoutineDay
/** Play button — deep indigo (the main action). */
private val PlayColor = RoutinePlay
/** Selected filter chip — keeps the brand indigo so it reads apart from play/days. */
private val FilterSelectedColor = BrandIndigoDark

enum class RoutineCategory(val labelRes: Int, val icon: ImageVector) {
    MORNING(R.string.routines_category_morning, SmartHomeIcons.MorningRoutine),
    NIGHT(R.string.routines_category_night, SmartHomeIcons.NightRoutine),
    COMFORT(R.string.routines_category_comfort, SmartHomeIcons.ComfortRoutine),
    SECURITY(R.string.routines_category_security, SmartHomeIcons.SecurityRoutine),
}

data class RoutineUi(
    val id: String,
    val name: String,
    val category: RoutineCategory,
    val time: String,
    val actions: List<String>,
    val extraActions: Int,
    /** 7 flags, Monday-first (L M M J V S D). */
    val days: List<Boolean>,
    val favorite: Boolean,
)

@Composable
fun RoutinesScreen(
    onProfileClick: () -> Unit = {},
    onAddRoutine: () -> Unit = {},
    routines: List<RoutineUi> = sampleRoutines,
) {
    var selectedCategory by remember { mutableStateOf<RoutineCategory?>(null) }
    val visibleRoutines = remember(selectedCategory, routines) {
        if (selectedCategory == null) routines
        else routines.filter { it.category == selectedCategory }
    }

    MainShellScaffold(
        title = stringResource(R.string.nav_routines),
        onSearchClick = {},
        onProfileClick = onProfileClick,
        floatingActionButton = {
            PhoneQuickAddFab(
                onClick = onAddRoutine,
                modifier = Modifier.navigationBarsPadding(),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
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
                CategoryFilter(
                    routines = routines,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it },
                )
            }
            items(items = visibleRoutines, key = { it.id }) { routine ->
                RoutineCard(routine = routine)
            }
        }
    }
}

@Composable
private fun CategoryFilter(
    routines: List<RoutineUi>,
    selectedCategory: RoutineCategory?,
    onCategorySelected: (RoutineCategory?) -> Unit,
) {
    val presentCategories = remember(routines) {
        RoutineCategory.entries.filter { cat -> routines.any { it.category == cat } }
    }
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
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            FilterChip(
                label = stringResource(R.string.routines_filter_all),
                count = routines.size,
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
            )
            presentCategories.forEach { category ->
                FilterChip(
                    label = stringResource(category.labelRes),
                    count = routines.count { it.category == category },
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                )
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
) {
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
private fun RoutineCard(routine: RoutineUi) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: icon + title/category + favorite star
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

            // Time
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

            // Actions
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

            // Days + play
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DaysRow(days = routine.days, modifier = Modifier.weight(1f))
                PlayButton(onClick = {})
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

private val sampleRoutines = listOf(
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
