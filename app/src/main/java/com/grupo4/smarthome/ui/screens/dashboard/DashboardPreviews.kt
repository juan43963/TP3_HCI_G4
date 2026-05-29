package com.grupo4.smarthome.ui.screens.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.grupo4.smarthome.ui.screens.dashboard.layout.TabletDashboardLayout
import com.grupo4.smarthome.ui.screens.dashboard.layout.phone.PhoneDashboardLayout
import com.grupo4.smarthome.ui.screens.dashboard.layout.phone.PhoneHomeDrawerContent
import com.grupo4.smarthome.ui.theme.SmartHomeTheme

@Preview(
    name = "Dashboard Tablet Horizontal - Poblado",
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240",
)
@Composable
private fun DashboardTabletLandscapePopulatedPreview() {
    DashboardTabletPreview(state = DashboardPreviewData.populated)
}

@Preview(
    name = "Dashboard Tablet Horizontal - Casa vacía",
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240",
)
@Composable
private fun DashboardTabletLandscapeEmptyHomePreview() {
    DashboardTabletPreview(state = DashboardPreviewData.emptyHome)
}

@Preview(
    name = "Dashboard Tablet Horizontal - Sin favoritas",
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240",
)
@Composable
private fun DashboardTabletLandscapeNoFavoritesPreview() {
    DashboardTabletPreview(state = DashboardPreviewData.roomsWithoutFavoritesAndNoRoutines)
}

@Preview(
    name = "Dashboard Tablet Horizontal - Rutinas sin favoritas",
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240",
)
@Composable
private fun DashboardTabletLandscapeNoRoutineFavoritesPreview() {
    DashboardTabletPreview(state = DashboardPreviewData.roomsWithFavoritesNoRoutineFavorites)
}

@Preview(
    name = "Dashboard Tablet Vertical - Poblado",
    showBackground = true,
    device = "spec:width=800dp,height=1280dp,dpi=240",
)
@Composable
private fun DashboardTabletPortraitPopulatedPreview() {
    DashboardTabletPreview(state = DashboardPreviewData.populated)
}

@Preview(
    name = "Dashboard Tablet Vertical - Casa vacía",
    showBackground = true,
    device = "spec:width=800dp,height=1280dp,dpi=240",
)
@Composable
private fun DashboardTabletPortraitEmptyHomePreview() {
    DashboardTabletPreview(state = DashboardPreviewData.emptyHome)
}

@Preview(
    name = "Dashboard Tablet Vertical - Sin favoritas",
    showBackground = true,
    device = "spec:width=800dp,height=1280dp,dpi=240",
)
@Composable
private fun DashboardTabletPortraitNoFavoritesPreview() {
    DashboardTabletPreview(state = DashboardPreviewData.roomsWithoutFavoritesAndNoRoutines)
}

@Preview(
    name = "Dashboard Tablet Vertical - Rutinas sin favoritas",
    showBackground = true,
    device = "spec:width=800dp,height=1280dp,dpi=240",
)
@Composable
private fun DashboardTabletPortraitNoRoutineFavoritesPreview() {
    DashboardTabletPreview(state = DashboardPreviewData.roomsWithFavoritesNoRoutineFavorites)
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DashboardPhonePlaceholderPreview() {
    SmartHomeTheme(darkTheme = false) {
        PhoneDashboardPlaceholder()
    }
}

@Preview(
    name = "Dashboard Phone - Poblado",
    showBackground = true,
    device = "spec:width=393dp,height=852dp,dpi=440",
)
@Composable
private fun DashboardPhonePopulatedPreview() {
    DashboardPhonePreview(state = DashboardPreviewData.populated)
}

@Preview(
    name = "Dashboard Phone - Casa vacía",
    showBackground = true,
    device = "spec:width=393dp,height=852dp,dpi=440",
)
@Composable
private fun DashboardPhoneEmptyHomePreview() {
    DashboardPhonePreview(state = DashboardPreviewData.emptyHome)
}

@Preview(
    name = "Dashboard Phone - Sin favoritas",
    showBackground = true,
    device = "spec:width=393dp,height=852dp,dpi=440",
)
@Composable
private fun DashboardPhoneNoFavoritesPreview() {
    DashboardPhonePreview(state = DashboardPreviewData.roomsWithoutFavoritesAndNoRoutines)
}

@Preview(
    name = "Dashboard Phone - Rutinas sin favoritas",
    showBackground = true,
    device = "spec:width=393dp,height=852dp,dpi=440",
)
@Composable
private fun DashboardPhoneNoRoutineFavoritesPreview() {
    DashboardPhonePreview(state = DashboardPreviewData.roomsWithFavoritesNoRoutineFavorites)
}

@Preview(
    name = "Dashboard Phone - Drawer",
    showBackground = true,
    device = "spec:width=393dp,height=852dp,dpi=440",
)
@Composable
private fun DashboardPhoneDrawerPreview() {
    SmartHomeTheme(darkTheme = false) {
        PhoneHomeDrawerContent(
            state = DashboardPreviewData.populated,
            callbacks = DashboardCallbacks(),
            onClose = {},
        )
    }
}

@Composable
private fun DashboardTabletPreview(state: DashboardUiState) {
    SmartHomeTheme(darkTheme = false) {
        TabletDashboardLayout(
            state = state,
            presentation = state.toPresentationState(),
            callbacks = DashboardCallbacks(),
        )
    }
}

@Composable
private fun DashboardPhonePreview(state: DashboardUiState) {
    SmartHomeTheme(darkTheme = false) {
        PhoneDashboardLayout(
            state = state,
            presentation = state.toPresentationState(),
            callbacks = DashboardCallbacks(),
        )
    }
}
