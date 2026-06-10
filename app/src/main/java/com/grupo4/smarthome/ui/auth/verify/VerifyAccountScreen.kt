package com.grupo4.smarthome.ui.auth.verify

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.auth.components.AuthBrandLogo
import com.grupo4.smarthome.ui.auth.components.AuthRoundedField
import com.grupo4.smarthome.ui.theme.SmartHomeTheme

@Composable
fun VerifyAccountScreen(
    email: String,
    onVerifySuccess: () -> Unit,
    onChangeEmail: () -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: VerifyAccountViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.verified) {
        if (uiState.verified) {
            viewModel.consumeVerified()
            onVerifySuccess()
        }
    }

    VerifyAccountContent(
        uiState = uiState,
        email = email,
        onCodeChange = viewModel::onCodeChange,
        onVerify = viewModel::verify,
        onResend = { viewModel.resend(email) },
        onChangeEmail = onChangeEmail,
        onBackToLogin = onBackToLogin,
    )
}

@Composable
fun VerifyAccountContent(
    uiState: VerifyAccountUiState,
    email: String,
    onCodeChange: (String) -> Unit,
    onVerify: () -> Unit,
    onResend: () -> Unit,
    onChangeEmail: () -> Unit,
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
            EmailBadge()

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.verify_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.verify_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(4.dp))

            TextButton(onClick = onChangeEmail) {
                Text(
                    text = stringResource(R.string.verify_change_email),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            AuthRoundedField(
                value = uiState.code,
                onValueChange = onCodeChange,
                placeholder = stringResource(R.string.verify_field_placeholder),
                keyboardOptions = KeyboardOptions.Default,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.verify_field_helper),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

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
                onClick = onVerify,
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
                        if (uiState.isLoading) R.string.verify_cta_submitting
                        else R.string.verify_cta_submit
                    ),
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ResendFooter(onResendClick = onResend)

            TextButton(onClick = onBackToLogin) {
                Text(
                    text = stringResource(R.string.verify_back_to_login),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EmailBadge() {
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
            imageVector = Icons.Outlined.Email,
            contentDescription = stringResource(R.string.verify_icon_content_description),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp),
        )
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
                    append(stringResource(R.string.verify_resend_question))
                    append(" ")
                }
                withStyle(SpanStyle(color = primary, fontWeight = FontWeight.SemiBold)) {
                    append(stringResource(R.string.verify_resend_link))
                }
            },
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun VerifyAccountPreview() {
    SmartHomeTheme(darkTheme = false) {
        VerifyAccountContent(
            uiState = VerifyAccountUiState(),
            email = "juanperez@gmail.com",
            onCodeChange = {},
            onVerify = {},
            onResend = {},
            onChangeEmail = {},
            onBackToLogin = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun VerifyAccountDarkPreview() {
    SmartHomeTheme(darkTheme = true) {
        VerifyAccountContent(
            uiState = VerifyAccountUiState(),
            email = "juanperez@gmail.com",
            onCodeChange = {},
            onVerify = {},
            onResend = {},
            onChangeEmail = {},
            onBackToLogin = {},
        )
    }
}
