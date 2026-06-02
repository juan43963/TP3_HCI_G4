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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.CloudUpload
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.grupo4.smarthome.R
import com.grupo4.smarthome.ui.theme.ErrorRed
import com.grupo4.smarthome.ui.theme.SmartHomeIcons
import com.grupo4.smarthome.ui.theme.SmartHomeTheme

// Type options

internal enum class HomeTypeOption(@StringRes val labelRes: Int, val icon: ImageVector) {
    Casa(R.string.add_home_type_casa, SmartHomeIcons.Home),
    Departamento(R.string.add_home_type_departamento, SmartHomeIcons.Apartment),
    Oficina(R.string.add_home_type_oficina, SmartHomeIcons.Office),
    Otro(R.string.add_home_type_otro, Icons.Default.MoreHoriz),
}

// Entry point

/**
 * Drop-in overlay for [BoxScope] (same pattern as [AppDrawerOverlay]).
 * Phone: slides up as full-screen. Tablet: Material Dialog centered on screen.
 */
@Composable
fun BoxScope.AddHomeOverlay(
    visible: Boolean,
    isTablet: Boolean,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible && !isTablet,
        enter = slideInVertically(animationSpec = tween(300)) { it } + fadeIn(tween(220)),
        exit = slideOutVertically(animationSpec = tween(250)) { it } + fadeOut(tween(180)),
        modifier = Modifier.matchParentSize(),
    ) {
        PhoneAddHomeScreen(onDismiss = onDismiss)
    }

    if (visible && isTablet) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            TabletAddHomeDialogContent(onDismiss = onDismiss)
        }
    }
}

// Phone layout

@Composable
private fun PhoneAddHomeScreen(onDismiss: () -> Unit) {
    var homeName by rememberSaveable { mutableStateOf("") }
    var nameError by rememberSaveable { mutableStateOf(false) }
    var selectedPhotoIndex by rememberSaveable { mutableStateOf(0) }
    var selectedTypeIndex by rememberSaveable { mutableStateOf(0) }
    var shareEmail by rememberSaveable { mutableStateOf("") }

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
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AddHomeSectionLabel(stringResource(R.string.add_home_photo_section))
                    PhonePhotoRow(
                        selectedIndex = selectedPhotoIndex,
                        onSelect = { selectedPhotoIndex = it },
                    )
                }
                HomeNameField(
                    value = homeName,
                    error = nameError,
                    onValueChange = { homeName = it; if (nameError) nameError = false },
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AddHomeSectionLabel(stringResource(R.string.add_home_type_section))
                    HomeTypeChips(
                        selectedIndex = selectedTypeIndex,
                        onSelect = { selectedTypeIndex = it },
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AddHomeSectionLabel(stringResource(R.string.add_home_share_section))
                    HomeShareRow(
                        value = shareEmail,
                        onValueChange = { shareEmail = it },
                    )
                }
                AddHomeSubmitButton(
                    onSubmit = {
                        if (homeName.isBlank()) nameError = true else onDismiss()
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
private fun AddHomeSubmitButton(onSubmit: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onSubmit,
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
            text = stringResource(R.string.add_home_cta),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

// Tablet layout

@Composable
private fun TabletAddHomeDialogContent(onDismiss: () -> Unit) {
    var homeName by rememberSaveable { mutableStateOf("") }
    var nameError by rememberSaveable { mutableStateOf(false) }
    var selectedPhotoIndex by rememberSaveable { mutableStateOf(0) }
    var selectedTypeIndex by rememberSaveable { mutableStateOf(0) }
    var shareEmail by rememberSaveable { mutableStateOf("") }

    Surface(
        modifier = Modifier
            .widthIn(min = 560.dp, max = 800.dp)
            .heightIn(max = 560.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.background,
    ) {
        Row(modifier = Modifier.fillMaxHeight()) {
            // Left panel — photo selector
            Column(
                modifier = Modifier
                    .width(240.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AddHomeSectionLabel(stringResource(R.string.add_home_photo_section))
                TabletPhotoColumn(
                    selectedIndex = selectedPhotoIndex,
                    onSelect = { selectedPhotoIndex = it },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.weight(1f))
                UploadPhotoButton(modifier = Modifier.fillMaxWidth())
            }
            // Thin divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.outlineVariant),
            )
            // Right panel — form
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.add_home_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.add_home_subtitle),
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
                HomeNameField(
                    value = homeName,
                    error = nameError,
                    onValueChange = { homeName = it; if (nameError) nameError = false },
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AddHomeSectionLabel(stringResource(R.string.add_home_type_section))
                    HomeTypeChips(
                        selectedIndex = selectedTypeIndex,
                        onSelect = { selectedTypeIndex = it },
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AddHomeSectionLabel(stringResource(R.string.add_home_share_section))
                    HomeShareRow(
                        value = shareEmail,
                        onValueChange = { shareEmail = it },
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = {
                        if (homeName.isBlank()) nameError = true else onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.add_home_cta),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }
    }
}

// Shared form components

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
                    text = stringResource(R.string.add_home_name_placeholder),
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

@Composable
private fun HomeTypeChips(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val types = HomeTypeOption.entries
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            types.dropLast(1).forEachIndexed { idx, type ->
                HomeTypeChip(
                    selected = selectedIndex == idx,
                    icon = type.icon,
                    label = stringResource(type.labelRes),
                    onClick = { onSelect(idx) },
                )
            }
        }
        Row {
            val lastIdx = types.size - 1
            HomeTypeChip(
                selected = selectedIndex == lastIdx,
                icon = types.last().icon,
                label = stringResource(types.last().labelRes),
                onClick = { onSelect(lastIdx) },
            )
        }
    }
}

@Composable
private fun HomeTypeChip(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surface
    val contentColor = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurface
    val borderColor = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)

    Surface(
        modifier = modifier
            .height(36.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = contentColor,
            )
        }
    }
}

@Composable
private fun HomeShareRow(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            ),
        )
        Button(
            onClick = { /* visual only */ },
            modifier = Modifier.height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(
                text = stringResource(R.string.add_home_share_add_button),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// Photo selector components

private val photoIcons = listOf(
    SmartHomeIcons.Home,
    SmartHomeIcons.Apartment,
    SmartHomeIcons.Rooms,
    SmartHomeIcons.Office,
)

@Composable
private fun PhonePhotoRow(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        photoIcons.forEachIndexed { index, _ ->
            PhotoThumbnail(
                index = index,
                selected = selectedIndex == index,
                onClick = { onSelect(index) },
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f),
            )
        }
        UploadPhotoTile(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f),
        )
    }
}

@Composable
private fun TabletPhotoColumn(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        photoIcons.forEachIndexed { index, _ ->
            PhotoThumbnail(
                index = index,
                selected = selectedIndex == index,
                onClick = { onSelect(index) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
            )
        }
    }
}

@Composable
private fun PhotoThumbnail(
    index: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = when (index) {
        0 -> MaterialTheme.colorScheme.primaryContainer
        1 -> MaterialTheme.colorScheme.secondaryContainer
        2 -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .then(
                if (selected) Modifier.border(2.dp, primaryColor, RoundedCornerShape(10.dp))
                else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = photoIcons[index],
            contentDescription = stringResource(
                R.string.add_home_photo_content_description,
                index + 1,
            ),
            tint = if (selected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun UploadPhotoTile(modifier: Modifier = Modifier) {
    val strokeColor = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            .drawBehind {
                drawRoundRect(
                    color = strokeColor,
                    cornerRadius = CornerRadius(10.dp.toPx()),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f),
                    ),
                )
            }
            .clickable { /* visual only */ },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.CloudUpload,
            contentDescription = stringResource(R.string.add_home_upload_photo),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun UploadPhotoButton(modifier: Modifier = Modifier) {
    val strokeColor = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            .drawBehind {
                drawRoundRect(
                    color = strokeColor,
                    cornerRadius = CornerRadius(10.dp.toPx()),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f),
                    ),
                )
            }
            .clickable { /* visual only */ },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.CloudUpload,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Column {
                Text(
                    text = stringResource(R.string.add_home_upload_drag),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.add_home_upload_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// Previews

@Preview(name = "Phone – Light", showBackground = true, showSystemUi = true)
@Composable
private fun AddHomePhoneLightPreview() {
    SmartHomeTheme(darkTheme = false) {
        PhoneAddHomeScreen(onDismiss = {})
    }
}

@Preview(name = "Phone – Dark", showBackground = true, showSystemUi = true)
@Composable
private fun AddHomePhoneDarkPreview() {
    SmartHomeTheme(darkTheme = true) {
        PhoneAddHomeScreen(onDismiss = {})
    }
}

@Preview(name = "Tablet landscape", showBackground = true, widthDp = 1280, heightDp = 800)
@Composable
private fun AddHomeTabletLandscapePreview() {
    SmartHomeTheme(darkTheme = false) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            TabletAddHomeDialogContent(onDismiss = {})
        }
    }
}

@Preview(name = "Tablet portrait", showBackground = true, widthDp = 800, heightDp = 1280)
@Composable
private fun AddHomeTabletPortraitPreview() {
    SmartHomeTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            TabletAddHomeDialogContent(onDismiss = {})
        }
    }
}
