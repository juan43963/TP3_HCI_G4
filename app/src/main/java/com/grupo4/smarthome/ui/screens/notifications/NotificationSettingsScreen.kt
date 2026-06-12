package com.grupo4.smarthome.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.window.core.layout.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.components.MainShellScaffold
import com.grupo4.smarthome.ui.components.TabletMainSidebar
import com.grupo4.smarthome.ui.util.isPhoneLandscape
import com.grupo4.smarthome.ui.components.TabletSidebarDestination
import com.grupo4.smarthome.ui.screens.dashboard.components.DashboardTopBar
import com.grupo4.smarthome.ui.screens.dashboard.layout.DashboardScaffold
import com.grupo4.smarthome.ui.screens.profile.SectionCard
import com.grupo4.smarthome.ui.theme.SmartHomeIcons
import com.grupo4.smarthome.ui.theme.SmartHomeTheme

@Composable
fun NotificationSettingsScreen(
    windowSize: WindowSizeClass,
    preferences: NotificationPreferences,
    onPreferencesChange: (NotificationPreferences) -> Unit,
    onProfileClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onNavigateHome: () -> Unit = {},
    onNavigateRoutines: () -> Unit = {},
    onNavigateRooms: () -> Unit = {},
    onNavigateHistory: () -> Unit = {},
    onHomeSelectorClick: () -> Unit = {},
    onBack: () -> Unit = {},
    selectedHomeName: String = "Casa Principal",
    modifier: Modifier = Modifier,
) {
    val isTablet = windowSize.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    if (isTablet) {
        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            val portraitLike = maxWidth < 900.dp
            val railCompact = portraitLike || isPhoneLandscape(maxWidth, maxHeight)
            val contentPadding = if (portraitLike) 24.dp else 32.dp

            DashboardScaffold(
                sidebar = {
                    TabletMainSidebar(
                        selectedDestination = TabletSidebarDestination.Notifications,
                        compact = railCompact,
                        onNavigateHome = onNavigateHome,
                        onNavigateRoutines = onNavigateRoutines,
                        onNavigateRooms = onNavigateRooms,
                        onNavigateHistory = onNavigateHistory,
                    )
                },
                topBar = {
                    DashboardTopBar(
                        selectedHomeName = selectedHomeName,
                        onHomeSelectorClick = onHomeSelectorClick,
                        onSearchClick = onSearchClick,
                        onProfileClick = onProfileClick,
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
                    NotificationSettingsContent(
                        preferences = preferences,
                        onPreferencesChange = onPreferencesChange,
                        horizontalPadding = contentPadding,
                        showHeader = true,
                        itemModifier = Modifier.widthIn(max = 720.dp),
                    )
                },
            )
        }
    } else {
        MainShellScaffold(
            title = stringResource(R.string.notification_settings_title),
            onSearchClick = onSearchClick,
            onProfileClick = onProfileClick,
            onBackClick = onBack,
            modifier = modifier,
        ) { innerPadding ->
            NotificationSettingsContent(
                preferences = preferences,
                onPreferencesChange = onPreferencesChange,
                horizontalPadding = 16.dp,
                showHeader = false,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun NotificationSettingsContent(
    preferences: NotificationPreferences,
    onPreferencesChange: (NotificationPreferences) -> Unit,
    horizontalPadding: Dp,
    showHeader: Boolean,
    modifier: Modifier = Modifier,
    itemModifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = horizontalPadding, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        if (showHeader) {
            Column(modifier = itemModifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.notification_settings_title),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.notification_settings_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Text(
                text = stringResource(R.string.notification_settings_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = itemModifier.fillMaxWidth(),
            )
        }

        NotificationPreferenceSections(
            preferences = preferences,
            onPreferencesChange = onPreferencesChange,
            itemModifier = itemModifier,
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun NotificationPreferenceSections(
    preferences: NotificationPreferences,
    onPreferencesChange: (NotificationPreferences) -> Unit,
    modifier: Modifier = Modifier,
    itemModifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Box(modifier = itemModifier.fillMaxWidth()) {
            SectionCard(title = stringResource(R.string.notification_settings_section_general)) {
                NotificationToggleRow(
                    icon = SmartHomeIcons.Notifications,
                    title = stringResource(R.string.notification_settings_master_label),
                    description = stringResource(R.string.notification_settings_master_description),
                    checked = preferences.enabled,
                    enabled = true,
                    onCheckedChange = { onPreferencesChange(preferences.copy(enabled = it)) },
                )
            }
        }

        Box(modifier = itemModifier.fillMaxWidth()) {
            SectionCard(
                title = stringResource(R.string.notification_settings_section_types),
                description = stringResource(R.string.notification_settings_section_types_description),
            ) {
                NotificationCategory.entries.forEachIndexed { index, category ->
                    NotificationToggleRow(
                        icon = category.icon,
                        title = stringResource(category.labelRes),
                        description = stringResource(category.descriptionRes),
                        checked = preferences.isEnabled(category),
                        enabled = preferences.enabled,
                        onCheckedChange = {
                            onPreferencesChange(preferences.withCategory(category, it))
                        },
                    )
                    if (index != NotificationCategory.entries.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    }
                }
            }
        }

    }
}

@Composable
private fun NotificationToggleRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val contentAlpha = if (enabled) 1f else 0.45f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = contentAlpha)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = contentAlpha),
                modifier = Modifier.size(20.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
            ),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NotificationSettingsLightPreview() {
    SmartHomeTheme(darkTheme = false) {
        NotificationSettingsContent(
            preferences = NotificationPreferences(),
            onPreferencesChange = {},
            horizontalPadding = 16.dp,
            showHeader = false,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NotificationSettingsDarkPreview() {
    SmartHomeTheme(darkTheme = true) {
        NotificationSettingsContent(
            preferences = NotificationPreferences(enabled = true, devices = false),
            onPreferencesChange = {},
            horizontalPadding = 16.dp,
            showHeader = false,
        )
    }
}
