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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.components.MainShellScaffold
import com.grupo4.smarthome.ui.components.TabletMainSidebar
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
    onQuickAddClick: () -> Unit = {},
    selectedHomeName: String = "Casa Principal",
    modifier: Modifier = Modifier,
) {
    val isTablet = windowSize.widthSizeClass != WindowWidthSizeClass.Compact

    if (isTablet) {
        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            val portraitLike = maxWidth < 900.dp
            val contentPadding = if (portraitLike) 24.dp else 32.dp

            DashboardScaffold(
                sidebar = {
                    TabletMainSidebar(
                        selectedDestination = TabletSidebarDestination.Notifications,
                        compact = portraitLike,
                        onNavigateHome = onNavigateHome,
                        onNavigateRoutines = onNavigateRoutines,
                        onNavigateRooms = onNavigateRooms,
                        onNavigateHistory = onNavigateHistory,
                        onAddClick = onQuickAddClick,
                    )
                },
                topBar = {
                    DashboardTopBar(
                        selectedHomeName = selectedHomeName,
                        onHomeSelectorClick = onHomeSelectorClick,
                        onSearchClick = onSearchClick,
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

/**
 * Tarjetas de preferencias de notificación (General, Tipos, En la aplicación y la nota).
 * Se reutiliza tanto en la pantalla dedicada de teléfono como embebido en la
 * Configuración de tablet, para no duplicar la lógica de los toggles.
 */
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

        Box(modifier = itemModifier.fillMaxWidth()) {
            SectionCard(title = stringResource(R.string.notification_settings_section_in_app)) {
                NotificationToggleRow(
                    icon = SmartHomeIcons.Info,
                    title = stringResource(R.string.notification_settings_toast_label),
                    description = stringResource(R.string.notification_settings_toast_description),
                    checked = preferences.showInAppToast,
                    enabled = preferences.enabled,
                    onCheckedChange = { onPreferencesChange(preferences.copy(showInAppToast = it)) },
                )
            }
        }

        NotificationNote(modifier = itemModifier)
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
        )
    }
}

@Composable
private fun NotificationNote(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = SmartHomeIcons.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stringResource(R.string.notification_settings_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
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
