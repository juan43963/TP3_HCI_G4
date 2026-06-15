package com.grupo4.smarthome.ui.screens.selecthome

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.theme.ErrorRed
import com.grupo4.smarthome.ui.theme.SmartHomeIcons
import com.grupo4.smarthome.ui.theme.SmartHomeTheme

internal enum class HomeTypeOption(@StringRes val labelRes: Int, val icon: ImageVector) {
    Casa(R.string.add_home_type_casa, SmartHomeIcons.Home),
    Departamento(R.string.add_home_type_departamento, SmartHomeIcons.Apartment),
    Oficina(R.string.add_home_type_oficina, SmartHomeIcons.Office),
    Otro(R.string.add_home_type_otro, Icons.Default.MoreHoriz),
}

@Composable
fun BoxScope.AddHomeOverlay(
    visible: Boolean,
    isTablet: Boolean,
    onDismiss: () -> Unit,
    isCreating: Boolean = false,
    errorMessage: String? = null,
    onCreate: (name: String, typeIndex: Int, shareEmails: List<String>) -> Unit = { _, _, _ -> },
) {
    AnimatedVisibility(
        visible = visible && !isTablet,
        enter = slideInVertically(animationSpec = tween(300)) { it } + fadeIn(tween(220)),
        exit = slideOutVertically(animationSpec = tween(250)) { it } + fadeOut(tween(180)),
        modifier = Modifier.matchParentSize(),
    ) {
        PhoneAddHomeScreen(
            onDismiss = onDismiss,
            isCreating = isCreating,
            errorMessage = errorMessage,
            onCreate = onCreate,
        )
    }

    if (visible && isTablet) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                TabletAddHomeDialogContent(
                    maxHeight = maxHeight,
                    onDismiss = onDismiss,
                    isCreating = isCreating,
                    errorMessage = errorMessage,
                    onCreate = onCreate,
                )
            }
        }
    }
}

@Composable
private fun PhoneAddHomeScreen(
    onDismiss: () -> Unit,
    isCreating: Boolean,
    errorMessage: String?,
    onCreate: (name: String, typeIndex: Int, shareEmails: List<String>) -> Unit,
) {
    var homeName by rememberSaveable { mutableStateOf("") }
    var nameError by rememberSaveable { mutableStateOf(false) }
    var selectedTypeIndex by rememberSaveable { mutableStateOf(0) }
    var shareEmail by rememberSaveable { mutableStateOf("") }
    val invitedEmails = rememberSaveable(saver = invitedEmailsSaver) { mutableStateListOf<String>() }
    val onAddInvite = {
        val email = shareEmail.trim()
        if (isValidShareEmail(email) && invitedEmails.none { it.equals(email, ignoreCase = true) }) {
            invitedEmails.add(email)
            shareEmail = ""
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = { AddHomePhoneTopBar(onBack = onDismiss) },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.add_home_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AddHomeFormCard {
                    HomeNameField(
                        value = homeName,
                        error = nameError,
                        onValueChange = { homeName = it; if (nameError) nameError = false },
                        placeholderRes = R.string.add_home_name_placeholder_mobile,
                    )
                }
                AddHomeFormCard {
                    AddHomeSectionLabel(stringResource(R.string.add_home_type_section))
                    Spacer(modifier = Modifier.height(12.dp))
                    AddHomeTypeGrid(
                        selectedIndex = selectedTypeIndex,
                        onSelect = { selectedTypeIndex = it },
                    )
                }
                AddHomeFormCard {
                    AddHomeShareField(
                        value = shareEmail,
                        onValueChange = { shareEmail = it },
                        onAdd = onAddInvite,
                    )
                }
                AddHomeFormCard {
                    AddHomeInvitedList(
                        emails = invitedEmails,
                        onRemove = { invitedEmails.remove(it) },
                    )
                }
                AddHomeErrorText(errorMessage)
                AddHomeSubmitButton(
                    isCreating = isCreating,
                    onSubmit = {
                        if (homeName.isBlank()) nameError = true
                        else onCreate(homeName, selectedTypeIndex, collectInvited(invitedEmails, shareEmail))
                    },
                )
            }
        }
    }
}

@Composable
private fun AddHomePhoneTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = SmartHomeIcons.Back,
                contentDescription = stringResource(R.string.add_home_back_description),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Column {
            Text(
                text = stringResource(R.string.add_home_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.add_home_step),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AddHomeSubmitButton(
    isCreating: Boolean,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onSubmit,
        enabled = !isCreating,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Text(
            text = stringResource(
                if (isCreating) R.string.add_home_creating else R.string.add_home_cta
            ),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Composable
private fun AddHomeErrorText(message: String?) {
    if (message == null) return
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun TabletAddHomeDialogContent(
    maxHeight: Dp,
    onDismiss: () -> Unit,
    isCreating: Boolean,
    errorMessage: String?,
    onCreate: (name: String, typeIndex: Int, shareEmails: List<String>) -> Unit,
) {
    var homeName by rememberSaveable { mutableStateOf("") }
    var nameError by rememberSaveable { mutableStateOf(false) }
    var selectedTypeIndex by rememberSaveable { mutableStateOf(0) }
    var shareEmail by rememberSaveable { mutableStateOf("") }
    val invitedEmails = rememberSaveable(saver = invitedEmailsSaver) { mutableStateListOf<String>() }
    val onAddInvite = {
        val email = shareEmail.trim()
        if (isValidShareEmail(email) && invitedEmails.none { it.equals(email, ignoreCase = true) }) {
            invitedEmails.add(email)
            shareEmail = ""
        }
    }

    Surface(
        modifier = Modifier
            .widthIn(max = 760.dp)
            .fillMaxWidth()
            .heightIn(max = maxHeight),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = SmartHomeIcons.Back,
                        contentDescription = stringResource(R.string.add_home_back_description),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.add_home_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.add_home_step),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = SmartHomeIcons.Close,
                        contentDescription = stringResource(R.string.add_home_close_description),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = stringResource(R.string.add_home_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AddHomeFormCard {
                    HomeNameField(
                        value = homeName,
                        error = nameError,
                        onValueChange = { homeName = it; if (nameError) nameError = false },
                        placeholderRes = R.string.add_home_name_placeholder_mobile,
                    )
                }
                AddHomeFormCard {
                    AddHomeSectionLabel(stringResource(R.string.add_home_type_section))
                    Spacer(modifier = Modifier.height(8.dp))
                    AddHomeTypeGrid(
                        selectedIndex = selectedTypeIndex,
                        onSelect = { selectedTypeIndex = it },
                        cellHeight = 56.dp,
                    )
                }
                AddHomeFormCard {
                    AddHomeShareField(
                        value = shareEmail,
                        onValueChange = { shareEmail = it },
                        onAdd = onAddInvite,
                    )
                }
                AddHomeFormCard {
                    AddHomeInvitedList(
                        emails = invitedEmails,
                        onRemove = { invitedEmails.remove(it) },
                    )
                }
            }
            AddHomeErrorText(errorMessage)
            AddHomeSubmitButton(
                isCreating = isCreating,
                onSubmit = {
                    if (homeName.isBlank()) nameError = true
                    else onCreate(homeName, selectedTypeIndex, collectInvited(invitedEmails, shareEmail))
                },
            )
        }
    }
}

@Composable
private fun AddHomeSectionLabel(label: String, modifier: Modifier = Modifier) {
    Text(
        text = label,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    )
}

@Composable
private fun HomeNameField(
    value: String,
    error: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    @StringRes placeholderRes: Int = R.string.add_home_name_placeholder,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.add_home_name_label),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = stringResource(placeholderRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            singleLine = true,
            isError = error,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                errorBorderColor = ErrorRed,
            ),
        )
        if (error) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = SmartHomeIcons.Info,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = stringResource(R.string.add_home_name_error),
                    style = MaterialTheme.typography.bodySmall,
                    color = ErrorRed,
                )
            }
        }
    }
}

private fun isValidShareEmail(email: String): Boolean {
    val trimmed = email.trim()
    return trimmed.contains("@") && trimmed.substringAfter("@").contains(".")
}

@Composable
private fun AddHomeShareField(
    value: String,
    onValueChange: (String) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AddHomeSectionLabel(stringResource(R.string.add_home_share_section))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = stringResource(R.string.add_home_share_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                ),
            )
            Button(
                onClick = onAdd,
                enabled = isValidShareEmail(value),
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(
                    text = stringResource(R.string.add_home_share_add_button),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

@Composable
private fun AddHomeInvitedList(
    emails: List<String>,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AddHomeSectionLabel(stringResource(R.string.add_home_invited_section))
        if (emails.isEmpty()) {
            Text(
                text = stringResource(R.string.add_home_invited_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            emails.forEach { email ->
                AddHomeInvitedRow(email = email, onRemove = { onRemove(email) })
            }
        }
    }
}

@Composable
private fun AddHomeInvitedRow(email: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = email.take(1).uppercase(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = SmartHomeIcons.Delete,
                contentDescription = stringResource(R.string.add_home_invited_remove),
                tint = ErrorRed,
            )
        }
    }
}

@Composable
private fun AddHomeFormCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun AddHomeTypeGrid(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    cellHeight: Dp = 72.dp,
) {
    val types = HomeTypeOption.entries
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            types.take(2).forEachIndexed { idx, type ->
                AddHomeTypeGridCell(
                    selected = selectedIndex == idx,
                    icon = type.icon,
                    label = stringResource(type.labelRes),
                    onClick = { onSelect(idx) },
                    height = cellHeight,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            types.drop(2).forEachIndexed { idx, type ->
                AddHomeTypeGridCell(
                    selected = selectedIndex == idx + 2,
                    icon = type.icon,
                    label = stringResource(type.labelRes),
                    onClick = { onSelect(idx + 2) },
                    height = cellHeight,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun AddHomeTypeGridCell(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface
    val contentColor = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurface
    val borderWidth = if (selected) 2.dp else 1.dp
    val borderColor = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outlineVariant

    Surface(
        modifier = modifier
            .height(height)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = BorderStroke(borderWidth, borderColor),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = contentColor,
            )
        }
    }
}

private val invitedEmailsSaver = listSaver<SnapshotStateList<String>, String>(
    save = { it.toList() },
    restore = { it.toMutableStateList() },
)

private fun collectInvited(invited: List<String>, pending: String): List<String> = buildList {
    addAll(invited)
    val email = pending.trim()
    if (isValidShareEmail(email) && none { it.equals(email, ignoreCase = true) }) add(email)
}

