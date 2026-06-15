package com.grupo4.smarthome.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.window.core.layout.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.components.MainShellScaffold
import com.grupo4.smarthome.ui.screens.notifications.NotificationPreferences
import com.grupo4.smarthome.ui.theme.SmartHomeIcons
import com.grupo4.smarthome.ui.theme.SmartHomeTheme
import com.grupo4.smarthome.ui.theme.ThemePreference

@Composable
fun ProfileScreen(
    windowSize: WindowSizeClass,
    userName: String = "",
    userEmail: String = "",
    isSavingName: Boolean = false,
    onSaveName: (String) -> Unit = {},
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    onThemePreferenceChange: (ThemePreference) -> Unit = {},
    onChangePassword: () -> Unit = {},
    onLogout: () -> Unit = {},
    onProfileAvatarClick: () -> Unit = {},
    onOpenNotificationSettings: () -> Unit = {},
    notificationPreferences: NotificationPreferences = NotificationPreferences(),
    onNotificationPreferencesChange: (NotificationPreferences) -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    unreadNotificationCount: Int = 0,
    enteredFromHome: Boolean = false,
    onBack: () -> Unit = {},
    onNavigateHome: () -> Unit = {},
    onNavigateRooms: () -> Unit = {},
    onNavigateRoutines: () -> Unit = {},
    onNavigateHistory: () -> Unit = {},
    onAddHome: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (windowSize.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)) {
        TabletSettingsLayout(
            userName = userName,
            userEmail = userEmail,
            isSavingName = isSavingName,
            onSaveName = onSaveName,
            themePreference = themePreference,
            onThemePreferenceChange = onThemePreferenceChange,
            onChangePassword = onChangePassword,
            onLogout = onLogout,
            onProfileAvatarClick = onProfileAvatarClick,
            notificationPreferences = notificationPreferences,
            onNotificationPreferencesChange = onNotificationPreferencesChange,
            onNotificationsClick = onNotificationsClick,
            onSearchClick = onSearchClick,
            unreadNotificationCount = unreadNotificationCount,
            enteredFromHome = enteredFromHome,
            onNavigateHome = onNavigateHome,
            onNavigateRooms = onNavigateRooms,
            onNavigateRoutines = onNavigateRoutines,
            onNavigateHistory = onNavigateHistory,
            onAddHome = onAddHome,
            modifier = modifier,
        )
        return
    }
    PhoneProfileLayout(
        userName = userName,
        userEmail = userEmail,
        isSavingName = isSavingName,
        onSaveName = onSaveName,
        themePreference = themePreference,
        onThemePreferenceChange = onThemePreferenceChange,
        onChangePassword = onChangePassword,
        onLogout = onLogout,
        onProfileAvatarClick = onProfileAvatarClick,
        onOpenNotificationSettings = onOpenNotificationSettings,
        onNotificationsClick = onNotificationsClick,
        onSearchClick = onSearchClick,
        unreadNotificationCount = unreadNotificationCount,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
private fun PhoneProfileLayout(
    userName: String,
    userEmail: String,
    themePreference: ThemePreference,
    onThemePreferenceChange: (ThemePreference) -> Unit,
    onChangePassword: () -> Unit,
    onProfileAvatarClick: () -> Unit,
    isSavingName: Boolean = false,
    onSaveName: (String) -> Unit = {},
    onLogout: () -> Unit = {},
    onOpenNotificationSettings: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    unreadNotificationCount: Int = 0,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    MainShellScaffold(
        title = stringResource(R.string.settings_title),
        onSearchClick = onSearchClick,
        onProfileClick = onProfileAvatarClick,
        onNotificationsClick = onNotificationsClick,
        notificationCount = unreadNotificationCount,
        onBackClick = onBack,
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Header()
            ProfileSection(
                userName = userName,
                userEmail = userEmail,
                isSavingName = isSavingName,
                onSaveName = onSaveName,
            )
            SecuritySection(onChangePassword = onChangePassword)
            NotificationsSection(onOpenNotificationSettings = onOpenNotificationSettings)
            AccessibilitySection(
                themePreference = themePreference,
                onThemePreferenceChange = onThemePreferenceChange,
            )
            AccountSection(onLogout = onLogout)
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
internal fun AccountSection(onLogout: () -> Unit) {
    SectionCard(title = stringResource(R.string.settings_section_account)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onLogout)
                .padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = SmartHomeIcons.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.dashboard_logout),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error,
                )
                Text(
                    text = stringResource(R.string.settings_logout_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun Header() {
    Column {
        Text(
            text = stringResource(R.string.settings_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun SectionCard(
    title: String? = null,
    description: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (description != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (title != null || description != null) {
                Spacer(modifier = Modifier.height(16.dp))
            }
            content()
        }
    }
}

@Composable
private fun ProfileSection(
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

    SectionCard(title = stringResource(R.string.settings_section_profile)) {
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
internal fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun SecuritySection(onChangePassword: () -> Unit) {
    SectionCard(title = stringResource(R.string.settings_section_security)) {
        SettingRow(
            icon = Icons.Outlined.Lock,
            title = stringResource(R.string.settings_change_password),
            description = stringResource(R.string.settings_change_password_description),
            onClick = onChangePassword,
        )
    }
}

@Composable
private fun NotificationsSection(onOpenNotificationSettings: () -> Unit) {
    SectionCard(title = stringResource(R.string.notification_settings_title)) {
        SettingRow(
            icon = SmartHomeIcons.Notifications,
            title = stringResource(R.string.notification_settings_title),
            description = stringResource(R.string.notification_settings_subtitle),
            onClick = onOpenNotificationSettings,
        )
    }
}

@Composable
internal fun SettingRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
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
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AccessibilitySection(
    themePreference: ThemePreference,
    onThemePreferenceChange: (ThemePreference) -> Unit,
) {
    SectionCard(
        title = stringResource(R.string.settings_section_accessibility),
        description = stringResource(R.string.settings_section_accessibility_description),
    ) {
        Text(
            text = stringResource(R.string.settings_theme_label),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ThemeOption(
                icon = Icons.Outlined.WbSunny,
                label = stringResource(R.string.settings_theme_light),
                selected = themePreference == ThemePreference.LIGHT,
                onClick = { onThemePreferenceChange(ThemePreference.LIGHT) },
            )
            ThemeOption(
                icon = Icons.Outlined.DarkMode,
                label = stringResource(R.string.settings_theme_dark),
                selected = themePreference == ThemePreference.DARK,
                onClick = { onThemePreferenceChange(ThemePreference.DARK) },
            )
            ThemeOption(
                icon = Icons.Outlined.Settings,
                label = stringResource(R.string.settings_theme_system),
                selected = themePreference == ThemePreference.SYSTEM,
                onClick = { onThemePreferenceChange(ThemePreference.SYSTEM) },
            )
        }
    }
}

@Composable
private fun ThemeOption(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val border = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val bg = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            ),
        shape = RoundedCornerShape(14.dp),
        color = bg,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = border,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            RadioButton(
                selected = selected,
                onClick = null,
            )
        }
    }
}

