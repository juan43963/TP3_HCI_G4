package com.grupo4.smarthome.ui.auth.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.grupo4.smarthome.ui.auth.components.AuthRoundedField
import com.grupo4.smarthome.ui.theme.SmartHomeTheme

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: (String) -> Unit,
    viewModel: RegisterViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.registrationCompleted, uiState.registeredEmail) {
        val email = uiState.registeredEmail
        if (uiState.registrationCompleted && email != null) {
            viewModel.consumeRegistrationCompleted()
            onRegisterSuccess(email)
        }
    }

    RegisterContent(
        uiState = uiState,
        onNavigateToLogin = onNavigateToLogin,
        onFullNameChange = viewModel::onFullNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onNextStep = viewModel::onNextStep,
        onPreviousStep = viewModel::onPreviousStep,
        onRegister = viewModel::register,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun RegisterContent(
    uiState: RegisterUiState,
    onNavigateToLogin: () -> Unit,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onNextStep: () -> Unit,
    onPreviousStep: () -> Unit,
    onRegister: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxHeight()
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
            StepIndicator(currentStep = uiState.step)

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.register_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(
                    if (uiState.step == 1) R.string.register_subtitle_step1
                    else R.string.register_subtitle_step2
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.step == 1) {
                StepOneForm(
                    fullName = uiState.fullName,
                    onFullNameChange = onFullNameChange,
                    email = uiState.email,
                    onEmailChange = onEmailChange,
                )

                RegisterErrorText(errorMessage = uiState.errorMessage)

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryButton(
                    text = stringResource(R.string.register_cta_continue),
                    enabled = uiState.step1Valid,
                    onClick = onNextStep,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                StepTwoForm(
                    password = uiState.password,
                    onPasswordChange = onPasswordChange,
                    passwordVisible = passwordVisible,
                    onTogglePasswordVisible = { passwordVisible = !passwordVisible },
                    confirmPassword = uiState.confirmPassword,
                    onConfirmPasswordChange = onConfirmPasswordChange,
                    confirmPasswordVisible = confirmPasswordVisible,
                    onToggleConfirmPasswordVisible = {
                        confirmPasswordVisible = !confirmPasswordVisible
                    },
                )

                RegisterErrorText(errorMessage = uiState.errorMessage)

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SecondaryButton(
                        text = stringResource(R.string.register_cta_back),
                        onClick = onPreviousStep,
                        modifier = Modifier.weight(1f),
                    )
                    PrimaryButton(
                        text = stringResource(
                            if (uiState.isLoading) R.string.register_cta_creating
                            else R.string.register_cta_create
                        ),
                        enabled = uiState.step2Valid,
                        onClick = onRegister,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LoginFooter(onLoginClick = onNavigateToLogin)
        }

        Text(
            text = stringResource(R.string.register_tagline),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun RegisterErrorText(errorMessage: String?) {
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
private fun StepIndicator(currentStep: Int, modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val inactive = MaterialTheme.colorScheme.outlineVariant
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val mutedText = MaterialTheme.colorScheme.onSurfaceVariant
    val activeText = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            StepCircle(
                isActive = true,
                isCompleted = currentStep > 1,
                number = 1,
                activeColor = primary,
                inactiveColor = inactive,
                contentColor = onPrimary,
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .height(2.dp)
                    .background(if (currentStep > 1) primary else inactive),
            )
            StepCircle(
                isActive = currentStep >= 2,
                isCompleted = false,
                number = 2,
                activeColor = primary,
                inactiveColor = inactive,
                contentColor = onPrimary,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.register_step_personal),
                style = MaterialTheme.typography.bodySmall,
                color = if (currentStep == 1) activeText else mutedText,
                fontWeight = if (currentStep == 1) FontWeight.SemiBold else FontWeight.Normal,
            )
            Text(
                text = stringResource(R.string.register_step_password),
                style = MaterialTheme.typography.bodySmall,
                color = if (currentStep == 2) activeText else mutedText,
                fontWeight = if (currentStep == 2) FontWeight.SemiBold else FontWeight.Normal,
            )
        }
    }
}

@Composable
private fun StepCircle(
    isActive: Boolean,
    isCompleted: Boolean,
    number: Int,
    activeColor: androidx.compose.ui.graphics.Color,
    inactiveColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .background(
                color = if (isActive) activeColor else inactiveColor,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (isCompleted) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp),
            )
        } else {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = if (isActive) contentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun StepOneForm(
    fullName: String,
    onFullNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
) {
    AuthRoundedField(
        value = fullName,
        onValueChange = onFullNameChange,
        placeholder = stringResource(R.string.register_field_full_name),
    )
    Spacer(modifier = Modifier.height(12.dp))
    AuthRoundedField(
        value = email,
        onValueChange = onEmailChange,
        placeholder = stringResource(R.string.register_field_email),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
    )
}

@Composable
private fun StepTwoForm(
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisible: () -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    confirmPasswordVisible: Boolean,
    onToggleConfirmPasswordVisible: () -> Unit,
) {
    PasswordField(
        value = password,
        onValueChange = onPasswordChange,
        placeholder = stringResource(R.string.register_field_password),
        visible = passwordVisible,
        onToggleVisible = onTogglePasswordVisible,
    )
    Spacer(modifier = Modifier.height(12.dp))
    PasswordField(
        value = confirmPassword,
        onValueChange = onConfirmPasswordChange,
        placeholder = stringResource(R.string.register_field_confirm_password),
        visible = confirmPasswordVisible,
        onToggleVisible = onToggleConfirmPasswordVisible,
    )
}

@Composable
internal fun PasswordField(
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
                    contentDescription = stringResource(
                        if (visible) R.string.register_hide_password else R.string.register_show_password
                    ),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}

@Composable
private fun PrimaryButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
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
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
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
private fun LoginFooter(onLoginClick: () -> Unit) {
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
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RegisterStep1Preview() {
    SmartHomeTheme(darkTheme = false) {
        RegisterContent(
            uiState = RegisterUiState(),
            onNavigateToLogin = {},
            onFullNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onNextStep = {},
            onPreviousStep = {},
            onRegister = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RegisterStep1DarkPreview() {
    SmartHomeTheme(darkTheme = true) {
        RegisterContent(
            uiState = RegisterUiState(),
            onNavigateToLogin = {},
            onFullNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onNextStep = {},
            onPreviousStep = {},
            onRegister = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
