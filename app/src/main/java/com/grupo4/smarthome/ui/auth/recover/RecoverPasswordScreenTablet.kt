package com.grupo4.smarthome.ui.auth.recover

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

private val TabletFormMaxWidth = 560.dp

@Composable
fun RecoverPasswordScreenTablet(
    onLinkSent: (String) -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: RecoverPasswordViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RecoverPasswordTabletContent(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onSendCode = viewModel::sendCode,
        onResend = viewModel::onResend,
        onLinkSent = onLinkSent,
        onBackToLogin = onBackToLogin,
    )
}

@Composable
private fun RecoverPasswordTabletContent(
    uiState: RecoverPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSendCode: () -> Unit,
    onResend: () -> Unit,
    onLinkSent: (String) -> Unit,
    onBackToLogin: () -> Unit,
) {
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
            if (!uiState.codeSent) {
                IconBadgeTablet(icon = Icons.Outlined.Lock)
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.recover_title),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.recover_subtitle),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(32.dp))

                AuthFieldLabel(
                    text = stringResource(R.string.recover_field_email_label),
                    tablet = true,
                )
                Spacer(modifier = Modifier.height(8.dp))
                AuthRoundedField(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    placeholder = stringResource(R.string.recover_field_email_placeholder),
                    tablet = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                PrimaryButtonTablet(
                    text = stringResource(
                        if (uiState.isLoading) R.string.recover_cta_sending
                        else R.string.recover_cta_send_link
                    ),
                    enabled = uiState.canSubmit,
                    onClick = onSendCode,
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onBackToLogin) {
                    Text(
                        text = stringResource(R.string.recover_back_to_login),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                IconBadgeTablet(icon = Icons.Outlined.Email)
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.recover_sent_title),
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
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(32.dp))

                PrimaryButtonTablet(
                    text = stringResource(R.string.recover_cta_enter_code),
                    enabled = true,
                    onClick = { onLinkSent(uiState.email) },
                )

                Spacer(modifier = Modifier.height(14.dp))

                SecondaryButtonTablet(
                    text = stringResource(R.string.recover_back_to_login),
                    onClick = onBackToLogin,
                )

                Spacer(modifier = Modifier.height(14.dp))

                ResendFooterTablet(onResendClick = onResend)
            }
        }
    }
}

@Composable
private fun IconBadgeTablet(icon: androidx.compose.ui.graphics.vector.ImageVector) {
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
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(36.dp),
        )
    }
}

@Composable
private fun PrimaryButtonTablet(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
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
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SecondaryButtonTablet(
    text: String,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ResendFooterTablet(onResendClick: () -> Unit) {
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
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Preview(
    name = "Recover Tablet Landscape",
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240",
)
@Composable
private fun RecoverPasswordScreenTabletPreview() {
    SmartHomeTheme(darkTheme = false) {
        RecoverPasswordTabletContent(
            uiState = RecoverPasswordUiState(),
            onEmailChange = {},
            onSendCode = {},
            onResend = {},
            onLinkSent = {},
            onBackToLogin = {},
        )
    }
}

@Preview(
    name = "Recover Tablet Landscape Dark",
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240",
)
@Composable
private fun RecoverPasswordScreenTabletDarkPreview() {
    SmartHomeTheme(darkTheme = true) {
        RecoverPasswordTabletContent(
            uiState = RecoverPasswordUiState(codeSent = true, email = "juanperez@gmail.com"),
            onEmailChange = {},
            onSendCode = {},
            onResend = {},
            onLinkSent = {},
            onBackToLogin = {},
        )
    }
}
