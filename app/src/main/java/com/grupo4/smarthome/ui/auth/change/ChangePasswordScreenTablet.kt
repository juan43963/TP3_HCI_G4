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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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

private const val MIN_PASSWORD_LENGTH = 8
private val TabletFormMaxWidth = 560.dp

@Composable
fun ChangePasswordScreenTablet(
    email: String,
    onPasswordChanged: () -> Unit,
    onBackToLogin: () -> Unit,
) {
    var code by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmVisible by rememberSaveable { mutableStateOf(false) }

    val canSubmit = code.isNotBlank() &&
        password.length >= MIN_PASSWORD_LENGTH &&
        password == confirm

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
                value = code,
                onValueChange = { code = it.trim() },
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
                value = password,
                onValueChange = { password = it },
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
                value = confirm,
                onValueChange = { confirm = it },
                placeholder = stringResource(R.string.change_pwd_field_confirm_placeholder),
                visible = confirmVisible,
                onToggleVisible = { confirmVisible = !confirmVisible },
            )

            if (confirm.isNotEmpty() && confirm != password) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.change_pwd_mismatch),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onPasswordChanged,
                enabled = canSubmit,
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
                    text = stringResource(R.string.change_pwd_cta_submit),
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

@Preview(
    name = "Change Password Tablet Landscape",
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240",
)
@Composable
private fun ChangePasswordScreenTabletPreview() {
    SmartHomeTheme(darkTheme = false) {
        ChangePasswordScreenTablet(
            email = "juanperez@itba.edu.ar",
            onPasswordChanged = {},
            onBackToLogin = {},
        )
    }
}

@Preview(
    name = "Change Password Tablet Landscape Dark",
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240",
)
@Composable
private fun ChangePasswordScreenTabletDarkPreview() {
    SmartHomeTheme(darkTheme = true) {
        ChangePasswordScreenTablet(
            email = "juanperez@itba.edu.ar",
            onPasswordChanged = {},
            onBackToLogin = {},
        )
    }
}
