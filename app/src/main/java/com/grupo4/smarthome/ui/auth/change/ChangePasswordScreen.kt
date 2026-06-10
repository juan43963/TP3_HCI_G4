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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.auth.components.AuthBrandLogo
import com.grupo4.smarthome.ui.auth.components.AuthFieldLabel
import com.grupo4.smarthome.ui.auth.components.AuthRoundedField
import com.grupo4.smarthome.ui.theme.SmartHomeTheme

@Composable
fun ChangePasswordScreen(
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

    ChangePasswordContent(
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
fun ChangePasswordContent(
    uiState: ChangePasswordUiState,
    email: String,
    onCodeChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmVisible by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        AuthBrandLogo()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IconBadge()

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.change_pwd_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

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
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            AuthFieldLabel(text = stringResource(R.string.change_pwd_field_code_label))
            Spacer(modifier = Modifier.height(6.dp))
            AuthRoundedField(
                value = uiState.code,
                onValueChange = onCodeChange,
                placeholder = stringResource(R.string.change_pwd_field_code_placeholder),
            )

            Spacer(modifier = Modifier.height(16.dp))

            AuthFieldLabel(text = stringResource(R.string.change_pwd_field_new_label))
            Spacer(modifier = Modifier.height(6.dp))
            PasswordField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                placeholder = stringResource(R.string.change_pwd_field_new_placeholder),
                visible = passwordVisible,
                onToggleVisible = { passwordVisible = !passwordVisible },
            )

            Spacer(modifier = Modifier.height(16.dp))

            AuthFieldLabel(text = stringResource(R.string.change_pwd_field_confirm_label))
            Spacer(modifier = Modifier.height(6.dp))
            PasswordField(
                value = uiState.confirm,
                onValueChange = onConfirmChange,
                placeholder = stringResource(R.string.change_pwd_field_confirm_placeholder),
                visible = confirmVisible,
                onToggleVisible = { confirmVisible = !confirmVisible },
            )

            if (uiState.showMismatch) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.change_pwd_mismatch),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(uiState.errorMessage),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onSubmit,
                enabled = uiState.canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
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
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onBackToLogin) {
                Text(
                    text = stringResource(R.string.change_pwd_back_to_login),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun IconBadge() {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.LockReset,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
private fun PasswordField(
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ChangePasswordLightPreview() {
    SmartHomeTheme(darkTheme = false) {
        ChangePasswordContent(
            uiState = ChangePasswordUiState(),
            email = "juanperez@itba.edu.ar",
            onCodeChange = {},
            onPasswordChange = {},
            onConfirmChange = {},
            onSubmit = {},
            onBackToLogin = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ChangePasswordDarkPreview() {
    SmartHomeTheme(darkTheme = true) {
        ChangePasswordContent(
            uiState = ChangePasswordUiState(),
            email = "juanperez@itba.edu.ar",
            onCodeChange = {},
            onPasswordChange = {},
            onConfirmChange = {},
            onSubmit = {},
            onBackToLogin = {},
        )
    }
}
