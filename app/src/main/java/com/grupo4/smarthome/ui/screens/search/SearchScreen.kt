package com.grupo4.smarthome.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.grupo4.smarthome.R
import com.grupo4.smarthome.data.model.Room
import com.grupo4.smarthome.data.model.Routine
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

@Composable
fun SearchScreen(
    windowSize: WindowSizeClass,
    state: SearchUiState,
    rooms: List<Room>,
    routines: List<Routine>,
    callbacks: SearchCallbacks,
    modifier: Modifier = Modifier,
    roomsErrorMessage: String? = null,
    deviceActionResult: String? = null,
    routinesErrorMessage: String? = null,
    routineInfoMessage: String? = null,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val executedTemplate = stringResource(R.string.routines_executed)

    LaunchedEffect(roomsErrorMessage) {
        roomsErrorMessage?.let {
            snackbarHostState.showSnackbar(it)
            callbacks.onDismissRoomError()
        }
    }
    LaunchedEffect(deviceActionResult) {
        deviceActionResult?.let {
            snackbarHostState.showSnackbar(it)
            callbacks.onConsumeDeviceActionResult()
        }
    }
    LaunchedEffect(routinesErrorMessage) {
        routinesErrorMessage?.let {
            snackbarHostState.showSnackbar(it)
            callbacks.onDismissRoutineError()
        }
    }
    LaunchedEffect(routineInfoMessage) {
        routineInfoMessage?.let {
            snackbarHostState.showSnackbar(executedTemplate.format(it))
            callbacks.onDismissRoutineInfo()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SearchTopBar(
                query = state.searchQuery,
                onQueryChange = callbacks.onQueryChange,
                onBack = callbacks.onBack,
                isTablet = windowSize.isWidthAtLeastBreakpoint(
                    WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        SearchContent(
            windowSize = windowSize,
            state = state,
            rooms = rooms,
            routines = routines,
            callbacks = callbacks,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }
}

@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    isTablet: Boolean,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(
                horizontal = if (isTablet) 32.dp else 16.dp,
                vertical = if (isTablet) 20.dp else 12.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(48.dp),
        ) {
            Icon(
                imageVector = SmartHomeIcons.Back,
                contentDescription = stringResource(R.string.search_back_content_description),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = if (isTablet) 64.dp else 56.dp)
                .widthIn(max = 720.dp)
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shadowElevation = 2.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = SmartHomeIcons.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = 18.dp, end = 10.dp)
                        .size(22.dp),
                )
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (query.isBlank()) {
                                Text(
                                    text = stringResource(R.string.search_placeholder),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            innerTextField()
                        }
                    },
                )
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = SmartHomeIcons.Close,
                            contentDescription = stringResource(R.string.search_clear_content_description),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    Box(modifier = Modifier.size(8.dp))
                }
            }
        }
    }
}
