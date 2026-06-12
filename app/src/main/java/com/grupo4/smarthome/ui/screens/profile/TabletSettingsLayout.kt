package com.grupo4.smarthome.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.components.LocalOpenDrawer
import com.grupo4.smarthome.ui.screens.dashboard.DashboardCallbacks
import com.grupo4.smarthome.ui.screens.dashboard.components.DashboardTopBar
import com.grupo4.smarthome.ui.screens.dashboard.layout.DashboardScaffold
import com.grupo4.smarthome.ui.screens.dashboard.layout.TabletSidebar
import com.grupo4.smarthome.ui.util.isPhoneLandscape
import com.grupo4.smarthome.ui.screens.notifications.NotificationPreferenceSections
import com.grupo4.smarthome.ui.screens.notifications.NotificationPreferences
import com.grupo4.smarthome.ui.screens.selecthome.layout.SelectHomeSidebar
import com.grupo4.smarthome.ui.screens.selecthome.layout.SelectHomeTopBar
import com.grupo4.smarthome.ui.theme.SmartHomeIcons
import com.grupo4.smarthome.ui.theme.SmartHomeTheme
import com.grupo4.smarthome.ui.theme.ThemePreference

private enum class SettingsSection { PROFILE, SECURITY, NOTIFICATIONS, ACCESSIBILITY }

@Composable
fun TabletSettingsLayout(
    userName: String,
    userEmail: String,
    isSavingName: Boolean = false,
    onSaveName: (String) -> Unit = {},
    themePreference: ThemePreference,
    onThemePreferenceChange: (ThemePreference) -> Unit,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit = {},
    onProfileAvatarClick: () -> Unit,
    notificationPreferences: NotificationPreferences = NotificationPreferences(),
    onNotificationPreferencesChange: (NotificationPreferences) -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    unreadNotificationCount: Int = 0,
    enteredFromHome: Boolean,
    onNavigateHome: () -> Unit,
    onNavigateRooms: () -> Unit,
    onNavigateRoutines: () -> Unit,
    onNavigateHistory: () -> Unit = {},
    onAddHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val openDrawer = LocalOpenDrawer.current
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val phoneLandscape = isPhoneLandscape(maxWidth, maxHeight)
        val portraitLike = maxWidth < 900.dp
        val contentPadding = if (portraitLike) 24.dp else 32.dp

        DashboardScaffold(
            sidebar = {
                if (enteredFromHome) {
                    TabletSidebar(
                        callbacks = DashboardCallbacks(
                            onNavigateHome = onNavigateHome,
                            onNavigateRooms = onNavigateRooms,
                            onNavigateRoutines = onNavigateRoutines,
                            onHistoryClick = onNavigateHistory,
                        ),
                        compact = portraitLike,
                        iconOnly = phoneLandscape,
                    )
                } else {
                    SelectHomeSidebar(
                        compact = portraitLike,
                        onSettingsClick = {},
                        onAddHome = onAddHome,
                        settingsSelected = true,
                    )
                }
            },
            topBar = {
                val topBarModifier = Modifier.padding(
                    start = contentPadding,
                    top = 24.dp,
                    end = contentPadding,
                    bottom = 14.dp,
                )
                if (enteredFromHome) {
                    DashboardTopBar(
                        selectedHomeName = "Departamento",
                        onHomeSelectorClick = openDrawer,
                        onSearchClick = {},
                        onProfileClick = onProfileAvatarClick,
                        onNotificationsClick = onNotificationsClick,
                        notificationCount = unreadNotificationCount,
                        useCompactHomeMenu = true,
                        modifier = topBarModifier,
                    )
                } else {
                    SelectHomeTopBar(
                        onProfileClick = onProfileAvatarClick,
                        modifier = topBarModifier,
                    )
                }
            },
            content = {
                SettingsContent(
                    userName = userName,
                    userEmail = userEmail,
                    isSavingName = isSavingName,
                    onSaveName = onSaveName,
                    themePreference = themePreference,
                    onThemePreferenceChange = onThemePreferenceChange,
                    onChangePassword = onChangePassword,
                    onLogout = onLogout,
                    notificationPreferences = notificationPreferences,
                    onNotificationPreferencesChange = onNotificationPreferencesChange,
                    contentPadding = contentPadding,
                    twoPane = !portraitLike,
                )
            },
        )
    }
}

@Composable
private fun SettingsContent(
    userName: String,
    userEmail: String,
    isSavingName: Boolean,
    onSaveName: (String) -> Unit,
    themePreference: ThemePreference,
    onThemePreferenceChange: (ThemePreference) -> Unit,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit,
    notificationPreferences: NotificationPreferences,
    onNotificationPreferencesChange: (NotificationPreferences) -> Unit,
    contentPadding: androidx.compose.ui.unit.Dp,
    twoPane: Boolean,
) {
    var selectedSection by remember { mutableStateOf(SettingsSection.ACCESSIBILITY) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = contentPadding)
            .padding(bottom = 28.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.settings_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (twoPane) {
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                SettingsNavList(
                    selectedSection = selectedSection,
                    onSectionSelected = { selectedSection = it },
                    modifier = Modifier.width(300.dp),
                )
                SettingsDetailPane(
                    selectedSection = selectedSection,
                    userName = userName,
                    userEmail = userEmail,
                    isSavingName = isSavingName,
                    onSaveName = onSaveName,
                    themePreference = themePreference,
                    onThemePreferenceChange = onThemePreferenceChange,
                    onChangePassword = onChangePassword,
                    notificationPreferences = notificationPreferences,
                    onNotificationPreferencesChange = onNotificationPreferencesChange,
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            SettingsNavList(
                selectedSection = selectedSection,
                onSectionSelected = { selectedSection = it },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(24.dp))
            SettingsDetailPane(
                selectedSection = selectedSection,
                userName = userName,
                userEmail = userEmail,
                isSavingName = isSavingName,
                onSaveName = onSaveName,
                themePreference = themePreference,
                onThemePreferenceChange = onThemePreferenceChange,
                onChangePassword = onChangePassword,
                notificationPreferences = notificationPreferences,
                onNotificationPreferencesChange = onNotificationPreferencesChange,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        AccountSection(onLogout = onLogout)
    }
}

@Composable
private fun SettingsNavList(
    selectedSection: SettingsSection,
    onSectionSelected: (SettingsSection) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.settings_list_header),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SettingsNavItem(
                icon = SmartHomeIcons.Profile,
                title = stringResource(R.string.settings_section_profile),
                subtitle = stringResource(R.string.settings_profile_subtitle),
                selected = selectedSection == SettingsSection.PROFILE,
                onClick = { onSectionSelected(SettingsSection.PROFILE) },
            )
            SettingsNavItem(
                icon = Icons.Outlined.Lock,
                title = stringResource(R.string.settings_section_security),
                subtitle = stringResource(R.string.settings_security_subtitle),
                selected = selectedSection == SettingsSection.SECURITY,
                onClick = { onSectionSelected(SettingsSection.SECURITY) },
            )
            SettingsNavItem(
                icon = SmartHomeIcons.Notifications,
                title = stringResource(R.string.notification_settings_title),
                subtitle = stringResource(R.string.notification_settings_section_settings_subtitle),
                selected = selectedSection == SettingsSection.NOTIFICATIONS,
                onClick = { onSectionSelected(SettingsSection.NOTIFICATIONS) },
            )
            SettingsNavItem(
                icon = Icons.Outlined.Contrast,
                title = stringResource(R.string.settings_section_accessibility),
                subtitle = stringResource(R.string.settings_accessibility_subtitle),
                selected = selectedSection == SettingsSection.ACCESSIBILITY,
                onClick = { onSectionSelected(SettingsSection.ACCESSIBILITY) },
            )
        }
    }
}

@Composable
private fun SettingsNavItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val titleColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.Tab),
        shape = RoundedCornerShape(16.dp),
        color = background,
        shadowElevation = if (selected) 0.dp else 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (selected) MaterialTheme.colorScheme.surface
                        else MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingsDetailPane(
    selectedSection: SettingsSection,
    userName: String,
    userEmail: String,
    isSavingName: Boolean,
    onSaveName: (String) -> Unit,
    themePreference: ThemePreference,
    onThemePreferenceChange: (ThemePreference) -> Unit,
    onChangePassword: () -> Unit,
    notificationPreferences: NotificationPreferences,
    onNotificationPreferencesChange: (NotificationPreferences) -> Unit,
    modifier: Modifier = Modifier,
) {
    val (titleRes, subtitleRes) = when (selectedSection) {
        SettingsSection.PROFILE ->
            R.string.settings_section_profile to R.string.settings_profile_subtitle
        SettingsSection.SECURITY ->
            R.string.settings_section_security to R.string.settings_security_subtitle
        SettingsSection.NOTIFICATIONS ->
            R.string.notification_settings_title to R.string.notification_settings_subtitle
        SettingsSection.ACCESSIBILITY ->
            R.string.settings_section_accessibility to R.string.settings_section_accessibility_description
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(subtitleRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        when (selectedSection) {
            SettingsSection.PROFILE -> ProfileDetailCard(
                userName = userName,
                userEmail = userEmail,
                isSavingName = isSavingName,
                onSaveName = onSaveName,
            )
            SettingsSection.SECURITY -> SecurityDetailCard(onChangePassword = onChangePassword)
            SettingsSection.NOTIFICATIONS -> NotificationPreferenceSections(
                preferences = notificationPreferences,
                onPreferencesChange = onNotificationPreferencesChange,
            )
            SettingsSection.ACCESSIBILITY -> ThemeDetailCard(
                themePreference = themePreference,
                onThemePreferenceChange = onThemePreferenceChange,
            )
        }
    }
}

@Composable
private fun ProfileDetailCard(
    userName: String,
    userEmail: String,
    isSavingName: Boolean,
    onSaveName: (String) -> Unit,
) {
    var editing by rememberSaveable { mutableStateOf(false) }
    var draftName by rememberSaveable(userName) { mutableStateOf(userName) }

    LaunchedEffect(isSavingName) {
        if (!isSavingName && editing && draftName.trim() == userName.trim()) {
            editing = false
        }
    }

    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = SmartHomeIcons.Profile,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                FieldLabel(stringResource(R.string.settings_field_name))
                if (editing) {
                    OutlinedTextField(
                        value = draftName,
                        onValueChange = { draftName = it },
                        singleLine = true,
                        enabled = !isSavingName,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )
                } else {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                FieldLabel(stringResource(R.string.settings_field_email))
                Text(
                    text = userEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (!editing) {
                IconButton(onClick = { draftName = userName; editing = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.profile_edit_name),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        if (editing) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                TextButton(
                    onClick = { editing = false; draftName = userName },
                    enabled = !isSavingName,
                ) {
                    Text(stringResource(R.string.profile_cancel))
                }
                Button(
                    onClick = { onSaveName(draftName) },
                    enabled = !isSavingName && draftName.isNotBlank() && draftName.trim() != userName.trim(),
                ) {
                    Text(
                        stringResource(
                            if (isSavingName) R.string.profile_saving else R.string.profile_save
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SecurityDetailCard(onChangePassword: () -> Unit) {
    SectionCard {
        SettingRow(
            icon = Icons.Outlined.Lock,
            title = stringResource(R.string.settings_change_password),
            description = stringResource(R.string.settings_change_password_description),
            onClick = onChangePassword,
        )
    }
}

@Composable
private fun ThemeDetailCard(
    themePreference: ThemePreference,
    onThemePreferenceChange: (ThemePreference) -> Unit,
) {
    SectionCard(
        title = stringResource(R.string.settings_theme_label),
        description = stringResource(R.string.settings_theme_card_description),
    ) {
        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ThemeOptionRow(
                icon = Icons.Outlined.WbSunny,
                label = stringResource(R.string.settings_theme_light),
                description = stringResource(R.string.settings_theme_light_description),
                selected = themePreference == ThemePreference.LIGHT,
                onClick = { onThemePreferenceChange(ThemePreference.LIGHT) },
            )
            ThemeOptionRow(
                icon = Icons.Outlined.DarkMode,
                label = stringResource(R.string.settings_theme_dark),
                description = stringResource(R.string.settings_theme_dark_description),
                selected = themePreference == ThemePreference.DARK,
                onClick = { onThemePreferenceChange(ThemePreference.DARK) },
            )
            ThemeOptionRow(
                icon = Icons.Outlined.Settings,
                label = stringResource(R.string.settings_theme_system),
                description = stringResource(R.string.settings_theme_system_description),
                selected = themePreference == ThemePreference.SYSTEM,
                onClick = { onThemePreferenceChange(ThemePreference.SYSTEM) },
            )
        }
    }
}

@Composable
private fun ThemeOptionRow(
    icon: ImageVector,
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val border = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val bg = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        shape = RoundedCornerShape(16.dp),
        color = bg,
        border = BorderStroke(width = if (selected) 1.5.dp else 1.dp, color = border),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            RadioButton(selected = selected, onClick = null)
        }
    }
}

@Preview(name = "Settings · desde casa (landscape)", showBackground = true, widthDp = 1280, heightDp = 800)
@Composable
private fun TabletSettingsFromHomePreview() {
    SmartHomeTheme(darkTheme = false) {
        TabletSettingsLayout(
            userName = "Juan Perez",
            userEmail = "juanperez@itba.edu.ar",
            themePreference = ThemePreference.SYSTEM,
            onThemePreferenceChange = {},
            onChangePassword = {},
            onProfileAvatarClick = {},
            enteredFromHome = true,
            onNavigateHome = {},
            onNavigateRooms = {},
            onNavigateRoutines = {},
            onAddHome = {},
        )
    }
}

@Preview(name = "Settings · desde hogares (landscape)", showBackground = true, widthDp = 1280, heightDp = 800)
@Composable
private fun TabletSettingsFromHomesPreview() {
    SmartHomeTheme(darkTheme = true) {
        TabletSettingsLayout(
            userName = "Juan Perez",
            userEmail = "juanperez@itba.edu.ar",
            themePreference = ThemePreference.DARK,
            onThemePreferenceChange = {},
            onChangePassword = {},
            onProfileAvatarClick = {},
            enteredFromHome = false,
            onNavigateHome = {},
            onNavigateRooms = {},
            onNavigateRoutines = {},
            onAddHome = {},
        )
    }
}
