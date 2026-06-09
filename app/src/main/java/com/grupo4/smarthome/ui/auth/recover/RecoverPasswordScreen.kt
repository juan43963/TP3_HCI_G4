package com.grupo4.smarthome.ui.auth.recover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
fun RecoverPasswordScreen(
    onLinkSent: (String) -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: RecoverPasswordViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RecoverPasswordContent(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onSendCode = viewModel::sendCode,
        onResend = viewModel::onResend,
        onLinkSent = onLinkSent,
        onBackToLogin = onBackToLogin,
    )
}

@Composable
fun RecoverPasswordContent(
    uiState: RecoverPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSendCode: () -> Unit,
    onResend: () -> Unit,
    onLinkSent: (String) -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (!uiState.codeSent) {
                IconBadge(icon = Icons.Outlined.Lock)

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.recover_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.recover_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(24.dp))

                AuthFieldLabel(text = stringResource(R.string.recover_field_email_label))
                Spacer(modifier = Modifier.height(6.dp))
                AuthRoundedField(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    placeholder = stringResource(R.string.recover_field_email_placeholder),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )

                RecoverErrorText(errorMessage = uiState.errorMessage)

                Spacer(modifier = Modifier.height(20.dp))

                PrimaryButton(
                    text = stringResource(
                        if (uiState.isLoading) R.string.recover_cta_sending
                        else R.string.recover_cta_send_link
                    ),
                    enabled = uiState.canSubmit,
                    onClick = onSendCode,
                )

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onBackToLogin) {
                    Text(
                        text = stringResource(R.string.recover_back_to_login),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                IconBadge(icon = Icons.Outlined.Email)

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.recover_sent_title),
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
                            append(stringResource(R.string.recover_sent_body_prefix))
                            append(" ")
                        }
                        withStyle(SpanStyle(color = strong, fontWeight = FontWeight.SemiBold)) {
                            append(uiState.email)
                        }
                        append(". ")
                        withStyle(SpanStyle(color = muted)) {
                            append(stringResource(R.string.recover_sent_body_suffix))
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryButton(
                    text = stringResource(R.string.recover_cta_enter_code),
                    enabled = true,
                    onClick = { onLinkSent(uiState.email) },
                )

                Spacer(modifier = Modifier.height(12.dp))

                SecondaryButton(
                    text = stringResource(R.string.recover_back_to_login),
                    onClick = onBackToLogin,
                )

                Spacer(modifier = Modifier.height(12.dp))

                ResendFooter(onResendClick = onResend)
            }
        }
    }
}

@Composable
internal fun RecoverErrorText(errorMessage: String?) {
    if (errorMessage == null) return
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = errorMessage,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun IconBadge(icon: androidx.compose.ui.graphics.vector.ImageVector) {
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
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
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
        Text(text = text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        Text(text = text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ResendFooter(onResendClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    TextButton(onClick = onResendClick) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = muted)) {
                    append(stringResource(R.string.recover_resend_question))
                    append(" ")
                }
                withStyle(SpanStyle(color = primary, fontWeight = FontWeight.SemiBold)) {
                    append(stringResource(R.string.recover_resend_link))
                }
            },
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RecoverPasswordLightPreview() {
    SmartHomeTheme(darkTheme = false) {
        RecoverPasswordContent(
            uiState = RecoverPasswordUiState(),
            onEmailChange = {},
            onSendCode = {},
            onResend = {},
            onLinkSent = {},
            onBackToLogin = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RecoverPasswordDarkPreview() {
    SmartHomeTheme(darkTheme = true) {
        RecoverPasswordContent(
            uiState = RecoverPasswordUiState(codeSent = true, email = "juanperez@gmail.com"),
            onEmailChange = {},
            onSendCode = {},
            onResend = {},
            onLinkSent = {},
            onBackToLogin = {},
        )
    }
}
