package com.grupo4.smarthome.ui.auth.change

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.auth.components.AuthBrandLogo
import com.grupo4.smarthome.ui.auth.components.AuthFieldLabel
import com.grupo4.smarthome.ui.auth.components.AuthRoundedField
import com.grupo4.smarthome.ui.theme.SmartHomeTheme

private val TabletFormMaxWidth = 560.dp

@Composable
fun ChangePasswordScreenTablet(
    email: String,
    onPasswordChanged: () -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: ChangePasswordViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.passwordChanged) {
        if (uiState.passwordChanged) {
            viewModel.consumePasswordChanged()
            onPasswordChanged()
        }
    }

    ChangePasswordTabletContent(
        uiState = uiState,
        email = email,
        onCodeChange = viewModel::onCodeChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmChange = viewModel::onConfirmChange,
        onSubmit = viewModel::submit,
        onBackToLogin = onBackToLogin,
    )
}

@Composable
private fun ChangePasswordTabletContent(
    uiState: ChangePasswordUiState,
    email: String,
    onCodeChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackToLogin: () -> Unit,
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmVisible by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
    ) {
        AuthBrandLogo(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 40.dp, top = 28.dp),
            tablet = true,
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = TabletFormMaxWidth)
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IconBadgeTablet()
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.change_pwd_title),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(10.dp))

            val muted = MaterialTheme.colorScheme.onSurfaceVariant
            val strong = MaterialTheme.colorScheme.onBackground
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = muted)) {
                        append(stringResource(R.string.change_pwd_subtitle_prefix))
                        append(" ")
                    }
                    if (email.isNotBlank()) {
                        withStyle(SpanStyle(color = strong, fontWeight = FontWeight.SemiBold)) {
                            append(email)
                        }
                    }
                    withStyle(SpanStyle(color = muted)) {
                        append(" ")
                        append(stringResource(R.string.change_pwd_subtitle_suffix))
                    }
                },
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            AuthFieldLabel(
                text = stringResource(R.string.change_pwd_field_code_label),
                tablet = true,
            )
            Spacer(modifier = Modifier.height(8.dp))
            AuthRoundedField(
                value = uiState.code,
                onValueChange = onCodeChange,
                placeholder = stringResource(R.string.change_pwd_field_code_placeholder),
                tablet = true,
            )

            Spacer(modifier = Modifier.height(20.dp))

            AuthFieldLabel(
                text = stringResource(R.string.change_pwd_field_new_label),
                tablet = true,
            )
            Spacer(modifier = Modifier.height(8.dp))
            PasswordFieldTablet(
                value = uiState.password,
                onValueChange = onPasswordChange,
                placeholder = stringResource(R.string.change_pwd_field_new_placeholder),
                visible = passwordVisible,
                onToggleVisible = { passwordVisible = !passwordVisible },
            )

            Spacer(modifier = Modifier.height(20.dp))

            AuthFieldLabel(
                text = stringResource(R.string.change_pwd_field_confirm_label),
                tablet = true,
            )
            Spacer(modifier = Modifier.height(8.dp))
            PasswordFieldTablet(
                value = uiState.confirm,
                onValueChange = onConfirmChange,
                placeholder = stringResource(R.string.change_pwd_field_confirm_placeholder),
                visible = confirmVisible,
                onToggleVisible = { confirmVisible = !confirmVisible },
            )

            if (uiState.showMismatch) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.change_pwd_mismatch),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(uiState.errorMessage),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onSubmit,
                enabled = uiState.canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                ),
            ) {
                Text(
                    text = stringResource(
                        if (uiState.isLoading) R.string.change_pwd_cta_submitting
                        else R.string.change_pwd_cta_submit
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            TextButton(onClick = onBackToLogin) {
                Text(
                    text = stringResource(R.string.change_pwd_back_to_login),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun IconBadgeTablet() {
    Box(
        modifier = Modifier
            .size(72.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(20.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.LockReset,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(36.dp),
        )
    }
}

@Composable
private fun PasswordFieldTablet(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    visible: Boolean,
    onToggleVisible: () -> Unit,
) {
    AuthRoundedField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        tablet = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggleVisible) {
                Icon(
                    imageVector = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}

