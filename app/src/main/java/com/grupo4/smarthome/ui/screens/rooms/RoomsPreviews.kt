package com.grupo4.smarthome.ui.screens.rooms

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.grupo4.smarthome.data.model.DeviceType
import com.grupo4.smarthome.data.model.HomeType
import com.grupo4.smarthome.ui.screens.selecthome.SelectableHome
import com.grupo4.smarthome.ui.screens.dashboard.DashboardCallbacks
import com.grupo4.smarthome.ui.screens.dashboard.DashboardPreviewData
import com.grupo4.smarthome.ui.screens.dashboard.layout.phone.PhoneHomeDrawer
import com.grupo4.smarthome.ui.screens.rooms.dialogs.AddDevicesToRoomDialog
import com.grupo4.smarthome.ui.screens.rooms.dialogs.AddEditRoomDialog
import com.grupo4.smarthome.ui.screens.rooms.dialogs.DeleteDeviceDialog
import com.grupo4.smarthome.ui.screens.rooms.dialogs.DeleteRoomDialog
import com.grupo4.smarthome.ui.screens.rooms.dialogs.EditDeviceDialog
import com.grupo4.smarthome.ui.screens.rooms.layout.TabletRoomDetailLayout
import com.grupo4.smarthome.ui.screens.rooms.layout.TabletRoomsListDetailLayout
import com.grupo4.smarthome.ui.screens.rooms.layout.TabletRoomsLayout
import com.grupo4.smarthome.ui.screens.rooms.layout.phone.PhoneRoomsLayout
import com.grupo4.smarthome.ui.theme.SmartHomeTheme

private const val PhonePreviewDevice = "spec:width=393dp,height=852dp,dpi=440"

@Preview(
    name = "Rooms Tablet Horizontal List-Detail - Poblado",
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240",
)
@Composable
private fun RoomsTabletLandscapePopulatedPreview() {
    RoomsTabletListDetailPreview(state = RoomsPreviewData.populated)
}

@Preview(
    name = "Rooms Tablet Horizontal List-Detail - Living seleccionado",
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240",
)
@Composable
private fun RoomsTabletLandscapeLivingSelectedPreview() {
    RoomsTabletListDetailPreview(state = RoomsPreviewData.detailLiving)
}

@Preview(
    name = "Rooms Tablet Horizontal List-Detail - Editar dispositivos",
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240",
)
@Composable
private fun RoomsTabletLandscapeEditDevicesPreview() {
    RoomsTabletListDetailPreview(state = RoomsPreviewData.detailLivingEditingDevices)
}

@Preview(
    name = "Rooms Tablet Horizontal List-Detail - Dormitorio seleccionado",
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240",
)
@Composable
private fun RoomsTabletLandscapeBedroomSelectedPreview() {
    RoomsTabletListDetailPreview(state = RoomsPreviewData.detailBedroom)
}

@Preview(
    name = "Rooms Tablet Vertical - Poblado",
    showBackground = true,
    device = "spec:width=800dp,height=1280dp,dpi=240",
)
@Composable
private fun RoomsTabletPortraitPopulatedPreview() {
    RoomsTabletOverviewPreview(state = RoomsPreviewData.populated)
}

@Preview(
    name = "Rooms Tablet Horizontal List-Detail - Sin habitaciones",
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240",
)
@Composable
private fun RoomsTabletLandscapeEmptyPreview() {
    RoomsTabletListDetailPreview(state = RoomsPreviewData.emptyHome)
}

@Preview(
    name = "Rooms Tablet Horizontal List-Detail - Sin resultados",
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240",
)
@Composable
private fun RoomsTabletLandscapeNoResultsPreview() {
    RoomsTabletListDetailPreview(state = RoomsPreviewData.noResults)
}

@Preview(
    name = "Rooms Tablet Horizontal List-Detail - Sin dispositivos",
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240",
)
@Composable
private fun RoomsTabletLandscapeEmptyDevicesPreview() {
    RoomsTabletListDetailPreview(state = RoomsPreviewData.detailEmptyRoom)
}

@Preview(
    name = "Room Detail Tablet Vertical - Living",
    showBackground = true,
    device = "spec:width=800dp,height=1280dp,dpi=240",
)
@Composable
private fun RoomDetailTabletPortraitLivingPreview() {
    RoomsTabletDetailPreview(state = RoomsPreviewData.detailLiving)
}

@Preview(
    name = "Rooms Phone - Listado poblado",
    showBackground = true,
    device = PhonePreviewDevice,
)
@Composable
private fun RoomsPhonePopulatedPreview() {
    RoomsPhonePreview(state = RoomsPreviewData.populated)
}

@Preview(
    name = "Rooms Phone - Sin habitaciones",
    showBackground = true,
    device = PhonePreviewDevice,
)
@Composable
private fun RoomsPhoneEmptyPreview() {
    RoomsPhonePreview(state = RoomsPreviewData.emptyHome)
}

@Preview(
    name = "Rooms Phone - Sin resultados",
    showBackground = true,
    device = PhonePreviewDevice,
)
@Composable
private fun RoomsPhoneNoResultsPreview() {
    RoomsPhonePreview(state = RoomsPreviewData.noResults)
}

@Preview(
    name = "Rooms Phone - Editar habitaciones",
    showBackground = true,
    device = PhonePreviewDevice,
)
@Composable
private fun RoomsPhoneEditRoomsPreview() {
    RoomsPhonePreview(state = RoomsPreviewData.populated.copy(editRoomsMode = true))
}

@Preview(
    name = "Room Detail Phone - Living",
    showBackground = true,
    device = PhonePreviewDevice,
)
@Composable
private fun RoomDetailPhoneLivingPreview() {
    RoomsPhonePreview(state = RoomsPreviewData.detailLiving)
}

@Preview(
    name = "Room Detail Phone - Sin dispositivos",
    showBackground = true,
    device = PhonePreviewDevice,
)
@Composable
private fun RoomDetailPhoneEmptyDevicesPreview() {
    RoomsPhonePreview(state = RoomsPreviewData.detailEmptyRoom)
}

@Preview(
    name = "Room Detail Phone - Editar dispositivos",
    showBackground = true,
    device = PhonePreviewDevice,
)
@Composable
private fun RoomDetailPhoneEditDevicesPreview() {
    RoomsPhonePreview(state = RoomsPreviewData.detailLivingEditingDevices)
}

@Preview(
    name = "Rooms Phone - Drawer",
    showBackground = true,
    device = PhonePreviewDevice,
)
@Composable
private fun RoomsPhoneDrawerPreview() {
    SmartHomeTheme(darkTheme = false) {
        PhoneHomeDrawer(
            userName = "Usuario",
            homes = listOf(
                SelectableHome("1", "Casa Principal", roomsCount = 3, devicesCount = 8, type = HomeType.CASA),
                SelectableHome("2", "Departamento", roomsCount = 2, devicesCount = 4, type = HomeType.DEPARTAMENTO),
            ),
            activeHomeId = "1",
            onHomeSelected = {},
            onSeeMoreHomes = {},
            onAddHome = {},
            onSettings = {},
            onLogout = {},
            onClose = {},
        )
    }
}

@Preview(name = "Dialog - Agregar habitación", showBackground = true)
@Composable
private fun AddRoomDialogPreview() {
    SmartHomeTheme(darkTheme = false) {
        AddEditRoomDialog(
            room = null,
            onDismiss = {},
            onConfirm = { _, _ -> },
        )
    }
}

@Preview(name = "Dialog - Editar habitación", showBackground = true)
@Composable
private fun EditRoomDialogPreview() {
    val room = RoomsPreviewData.populated.rooms.first()
    SmartHomeTheme(darkTheme = false) {
        AddEditRoomDialog(
            room = room,
            onDismiss = {},
            onConfirm = { _, _ -> },
        )
    }
}

@Preview(name = "Dialog - Eliminar habitación", showBackground = true)
@Composable
private fun DeleteRoomDialogPreview() {
    SmartHomeTheme(darkTheme = false) {
        DeleteRoomDialog(
            roomName = "Living",
            onDismiss = {},
            onConfirm = {},
        )
    }
}

@Preview(name = "Dialog - Agregar dispositivos paso 1", showBackground = true)
@Composable
private fun AddDevicesDialogStepOnePreview() {
    SmartHomeTheme(darkTheme = false) {
        AddDevicesToRoomDialog(
            roomName = "Living",
            availableDeviceTypes = RoomsPreviewData.availableDeviceTypes,
            onDismiss = {},
            onConfirm = {},
        )
    }
}

@Preview(name = "Dialog - Agregar dispositivos paso 2", showBackground = true)
@Composable
private fun AddDevicesDialogStepTwoPreview() {
    SmartHomeTheme(darkTheme = false) {
        AddDevicesToRoomDialog(
            roomName = "Living",
            availableDeviceTypes = RoomsPreviewData.availableDeviceTypes,
            onDismiss = {},
            onConfirm = {},
            initialStep = 2,
            initialDevices = listOf(
                PendingDevice("preview-light", DeviceType.LIGHT, "Lámpara"),
                PendingDevice("preview-speaker", DeviceType.SPEAKER, "Parlante"),
                PendingDevice("preview-blinds", DeviceType.BLINDS, "Persiana"),
            ),
        )
    }
}

@Preview(name = "Dialog - Editar dispositivo", showBackground = true)
@Composable
private fun EditDeviceDialogPreview() {
    val device = RoomsPreviewData.detailLiving.rooms
        .first { it.id == "living" }
        .devices
        .first()
    SmartHomeTheme(darkTheme = false) {
        EditDeviceDialog(
            device = device,
            onDismiss = {},
            onConfirm = {},
        )
    }
}

@Preview(name = "Dialog - Eliminar dispositivo", showBackground = true)
@Composable
private fun DeleteDeviceDialogPreview() {
    SmartHomeTheme(darkTheme = false) {
        DeleteDeviceDialog(
            deviceName = "Lámpara principal",
            onDismiss = {},
            onConfirm = {},
        )
    }
}

@Preview(
    name = "Dialog Phone - Agregar habitación",
    showBackground = true,
    device = PhonePreviewDevice,
)
@Composable
private fun AddRoomDialogPhonePreview() {
    SmartHomeTheme(darkTheme = false) {
        AddEditRoomDialog(
            room = null,
            onDismiss = {},
            onConfirm = { _, _ -> },
        )
    }
}

@Preview(
    name = "Dialog Phone - Agregar dispositivos",
    showBackground = true,
    device = PhonePreviewDevice,
)
@Composable
private fun AddDevicesDialogPhonePreview() {
    SmartHomeTheme(darkTheme = false) {
        AddDevicesToRoomDialog(
            roomName = "Living",
            availableDeviceTypes = RoomsPreviewData.availableDeviceTypes,
            onDismiss = {},
            onConfirm = {},
        )
    }
}

@Preview(
    name = "Dialog Phone - Editar dispositivo",
    showBackground = true,
    device = PhonePreviewDevice,
)
@Composable
private fun EditDeviceDialogPhonePreview() {
    val device = RoomsPreviewData.detailLiving.rooms
        .first { it.id == "living" }
        .devices
        .first()
    SmartHomeTheme(darkTheme = false) {
        EditDeviceDialog(
            device = device,
            onDismiss = {},
            onConfirm = {},
        )
    }
}

@Preview(
    name = "Dialog Phone - Eliminar dispositivo",
    showBackground = true,
    device = PhonePreviewDevice,
)
@Composable
private fun DeleteDeviceDialogPhonePreview() {
    SmartHomeTheme(darkTheme = false) {
        DeleteDeviceDialog(
            deviceName = "Lámpara principal",
            onDismiss = {},
            onConfirm = {},
        )
    }
}

@Composable
private fun RoomsTabletListDetailPreview(state: RoomsUiState) {
    SmartHomeTheme(darkTheme = false) {
        TabletRoomsListDetailLayout(
            state = state,
            presentation = state.toPresentationState(),
            callbacks = RoomsCallbacks(),
        )
    }
}

@Composable
private fun RoomsPhonePreview(state: RoomsUiState) {
    SmartHomeTheme(darkTheme = false) {
        PhoneRoomsLayout(
            state = state,
            presentation = state.toPresentationState(),
            callbacks = RoomsCallbacks(),
        )
    }
}

@Composable
private fun RoomsTabletOverviewPreview(state: RoomsUiState) {
    SmartHomeTheme(darkTheme = false) {
        TabletRoomsLayout(
            state = state,
            presentation = state.toPresentationState(),
            callbacks = RoomsCallbacks(),
        )
    }
}

@Composable
private fun RoomsTabletDetailPreview(state: RoomsUiState) {
    SmartHomeTheme(darkTheme = false) {
        TabletRoomDetailLayout(
            state = state,
            presentation = state.toPresentationState(),
            callbacks = RoomsCallbacks(),
        )
    }
}
