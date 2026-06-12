package com.grupo4.smarthome.ui.screens.selecthome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.grupo4.smarthome.R
import com.grupo4.smarthome.data.model.SharedUser
import com.grupo4.smarthome.ui.components.ModalCloseButton
import com.grupo4.smarthome.ui.theme.ErrorBg
import com.grupo4.smarthome.ui.theme.ErrorRed
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

@Composable
internal fun HomeActionDialogs(
    dialog: HomeDialog?,
    isProcessing: Boolean,
    actionError: String?,
    sharedUsers: List<SharedUser>,
    isLoadingShared: Boolean,
    onDismiss: () -> Unit,
    onConfirmRename: (String) -> Unit,
    onConfirmDelete: () -> Unit,
    onConfirmShare: (String) -> Unit,
) {
    when (dialog) {
        is HomeDialog.Rename -> RenameHomeDialog(
            home = dialog.home,
            isProcessing = isProcessing,
            actionError = actionError,
            onDismiss = onDismiss,
            onConfirm = onConfirmRename,
        )
        is HomeDialog.Delete -> DeleteHomeDialog(
            home = dialog.home,
            isProcessing = isProcessing,
            actionError = actionError,
            onDismiss = onDismiss,
            onConfirm = onConfirmDelete,
        )
        is HomeDialog.Share -> ShareHomeDialog(
            isProcessing = isProcessing,
            actionError = actionError,
            sharedUsers = sharedUsers,
            isLoadingShared = isLoadingShared,
            onDismiss = onDismiss,
            onConfirm = onConfirmShare,
        )
        null -> Unit
    }
}

@Composable
private fun DialogCard(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background,
            shadowElevation = 6.dp,
        ) {
            Box {
                Column(modifier = Modifier.padding(24.dp)) {
                    content()
                }
                ModalCloseButton(
                    onDismiss = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                )
            }
        }
    }
}

@Composable
private fun RenameHomeDialog(
    home: SelectableHome,
    isProcessing: Boolean,
    actionError: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by rememberSaveable(home.id) { mutableStateOf(home.name) }
    DialogCard(onDismiss = onDismiss) {
        Text(
            text = stringResource(R.string.select_home_rename_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.select_home_rename_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))
        DialogFieldLabel(stringResource(R.string.select_home_rename_label))
        Spacer(modifier = Modifier.height(4.dp))
        DialogTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = null,
            enabled = !isProcessing,
        )
        DialogError(actionError)
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onDismiss, enabled = !isProcessing) {
                Text(stringResource(R.string.select_home_dialog_cancel))
            }
            Spacer(modifier = Modifier.width(8.dp))
            PrimaryDialogButton(
                text = stringResource(R.string.select_home_rename_confirm),
                enabled = !isProcessing && name.isNotBlank(),
                loading = isProcessing,
                onClick = { onConfirm(name) },
            )
        }
    }
}

@Composable
private fun DeleteHomeDialog(
    home: SelectableHome,
    isProcessing: Boolean,
    actionError: String?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    DialogCard(onDismiss = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(ErrorBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = SmartHomeIcons.Delete,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(28.dp),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.select_home_delete_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.select_home_delete_message, home.name),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            DialogError(actionError)
            Spacer(modifier = Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onDismiss, enabled = !isProcessing) {
                    Text(stringResource(R.string.select_home_dialog_cancel))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onConfirm,
                    enabled = !isProcessing,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    DialogButtonContent(
                        text = stringResource(R.string.select_home_delete_confirm),
                        loading = isProcessing,
                    )
                }
            }
        }
    }
}

@Composable
private fun ShareHomeDialog(
    isProcessing: Boolean,
    actionError: String?,
    sharedUsers: List<SharedUser>,
    isLoadingShared: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    DialogCard(onDismiss = onDismiss) {
        Text(
            text = stringResource(R.string.select_home_share_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.select_home_share_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))
        DialogFieldLabel(stringResource(R.string.select_home_share_label))
        Spacer(modifier = Modifier.height(4.dp))
        DialogTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = stringResource(R.string.select_home_share_placeholder),
            enabled = !isProcessing,
        )
        DialogError(actionError)
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(12.dp))
        DialogFieldLabel(stringResource(R.string.select_home_share_current, sharedUsers.size))
        Spacer(modifier = Modifier.height(8.dp))
        when {
            isLoadingShared -> CircularProgressIndicator(modifier = Modifier.size(24.dp))
            sharedUsers.isEmpty() -> Text(
                text = stringResource(R.string.select_home_share_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            else -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                sharedUsers.forEach { user -> SharedUserRow(user) }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onDismiss, enabled = !isProcessing) {
                Text(stringResource(R.string.select_home_share_close))
            }
            Spacer(modifier = Modifier.width(8.dp))
            PrimaryDialogButton(
                text = stringResource(R.string.select_home_share_confirm),
                enabled = !isProcessing && email.isNotBlank(),
                loading = isProcessing,
                onClick = {
                    onConfirm(email)
                    email = ""
                },
            )
        }
    }
}

@Composable
private fun SharedUserRow(user: SharedUser) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = user.name.ifBlank { user.email }.take(1).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                if (user.name.isNotBlank()) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DialogFieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun DialogTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String?,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = placeholder?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        singleLine = true,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        ),
    )
}

@Composable
private fun PrimaryDialogButton(
    text: String,
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        DialogButtonContent(text = text, loading = loading)
    }
}

@Composable
private fun DialogButtonContent(text: String, loading: Boolean) {
    if (loading) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    } else {
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DialogError(message: String?) {
    if (message != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = ErrorRed,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
