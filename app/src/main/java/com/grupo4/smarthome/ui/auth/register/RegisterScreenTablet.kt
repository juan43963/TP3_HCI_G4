package com.grupo4.smarthome.ui.auth.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.auth.components.AuthBrandLogo
import com.grupo4.smarthome.ui.auth.components.AuthRoundedField
import com.grupo4.smarthome.ui.theme.SmartHomeTheme

private val TabletFormMaxWidth = 620.dp
private val StepCircleSize = 40.dp

@Composable
fun RegisterScreenTablet(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
) {
    var step by rememberSaveable { mutableStateOf(1) }
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    val step1Valid = fullName.isNotBlank() && email.isNotBlank()
    val step2Valid = password.isNotBlank() && password == confirmPassword

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
            StepIndicatorTablet(currentStep = step)

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = stringResource(R.string.register_title),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(
                    if (step == 1) R.string.register_subtitle_step1
                    else R.string.register_subtitle_step2
                ),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(36.dp))

            if (step == 1) {
                AuthRoundedField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    placeholder = stringResource(R.string.register_field_full_name),
                )
                Spacer(modifier = Modifier.height(16.dp))
                AuthRoundedField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = stringResource(R.string.register_field_email),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )

                Spacer(modifier = Modifier.height(32.dp))

                PrimaryButtonTablet(
                    text = stringResource(R.string.register_cta_continue),
                    enabled = step1Valid,
                    onClick = { step = 2 },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                PasswordField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = stringResource(R.string.register_field_password),
                    visible = passwordVisible,
                    onToggleVisible = { passwordVisible = !passwordVisible },
                )
                Spacer(modifier = Modifier.height(16.dp))
                PasswordField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = stringResource(R.string.register_field_confirm_password),
                    visible = confirmPasswordVisible,
                    onToggleVisible = { confirmPasswordVisible = !confirmPasswordVisible },
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    SecondaryButtonTablet(
                        text = stringResource(R.string.register_cta_back),
                        onClick = { step = 1 },
                        modifier = Modifier.weight(1f),
                    )
                    PrimaryButtonTablet(
                        text = stringResource(R.string.register_cta_create),
                        enabled = step2Valid,
                        onClick = onRegisterSuccess,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            LoginFooterTablet(onLoginClick = onNavigateToLogin)
        }

        Text(
            text = stringResource(R.string.register_tagline),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun StepIndicatorTablet(currentStep: Int, modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val inactive = MaterialTheme.colorScheme.outlineVariant

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        StepItemTablet(
            number = 1,
            label = stringResource(R.string.register_step_personal),
            isActive = true,
            isCompleted = currentStep > 1,
            modifier = Modifier.width(160.dp),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(top = StepCircleSize / 2)
                .height(2.dp)
                .background(if (currentStep > 1) primary else inactive),
        )
        StepItemTablet(
            number = 2,
            label = stringResource(R.string.register_step_password),
            isActive = currentStep >= 2,
            isCompleted = false,
            modifier = Modifier.width(160.dp),
        )
    }
}

@Composable
private fun StepItemTablet(
    number: Int,
    label: String,
    isActive: Boolean,
    isCompleted: Boolean,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val inactive = MaterialTheme.colorScheme.outlineVariant
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(StepCircleSize)
                .background(
                    color = if (isActive) primary else inactive,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = onPrimary,
                    modifier = Modifier.size(22.dp),
                )
            } else {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isActive) onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isActive) MaterialTheme.colorScheme.onBackground
            else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PrimaryButtonTablet(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(60.dp),
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
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(60.dp),
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
private fun LoginFooterTablet(onLoginClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    TextButton(onClick = onLoginClick) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = muted)) {
                    append(stringResource(R.string.register_footer_question))
                    append(" ")
                }
                withStyle(SpanStyle(color = primary, fontWeight = FontWeight.SemiBold)) {
                    append(stringResource(R.string.register_footer_login))
                }
            },
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Preview(
    name = "Register Tablet Landscape",
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240",
)
@Composable
private fun RegisterScreenTabletPreview() {
    SmartHomeTheme(darkTheme = false) {
        RegisterScreenTablet(
            onNavigateToLogin = {},
            onRegisterSuccess = {},
        )
    }
}

@Preview(
    name = "Register Tablet Landscape Dark",
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240",
)
@Composable
private fun RegisterScreenTabletDarkPreview() {
    SmartHomeTheme(darkTheme = true) {
        RegisterScreenTablet(
            onNavigateToLogin = {},
            onRegisterSuccess = {},
        )
    }
}
