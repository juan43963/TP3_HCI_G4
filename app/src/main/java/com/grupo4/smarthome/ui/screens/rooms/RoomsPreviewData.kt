package com.grupo4.smarthome.ui.screens.rooms

import com.grupo4.smarthome.data.model.Device
import com.grupo4.smarthome.data.model.DeviceType
import com.grupo4.smarthome.data.model.Room
import com.grupo4.smarthome.data.model.RoomType

object RoomsPreviewData {
    val availableDeviceTypes = listOf(
        DeviceType.VACUUM,
        DeviceType.SPEAKER,
        DeviceType.FAUCET,
        DeviceType.BLINDS,
        DeviceType.LIGHT,
        DeviceType.OVEN,
        DeviceType.AC,
        DeviceType.DOOR,
        DeviceType.ALARM,
        DeviceType.FRIDGE,
    )

    val populated = RoomsUiState(
        rooms = listOf(
            Room(
                id = "living",
                name = "Living",
                type = RoomType.LIVING,
                isFavorite = true,
                devices = listOf(
                    Device(
                        id = "living-light",
                        name = "Lámpara principal",
                        type = DeviceType.LIGHT,
                        roomId = "living",
                        isOn = true,
                        brightness = 75,
                    ),
                    Device(
                        id = "living-speaker",
                        name = "Parlante",
                        type = DeviceType.SPEAKER,
                        roomId = "living",
                        isOn = true,
                        volume = 60,
                    ),
                    Device(
                        id = "living-ac",
                        name = "Aire acondicionado",
                        type = DeviceType.AC,
                        roomId = "living",
                        isOn = true,
                        temperature = 22,
                    ),
                    Device(
                        id = "living-door",
                        name = "Puerta",
                        type = DeviceType.DOOR,
                        roomId = "living",
                        locked = true,
                    ),
                ),
            ),
            Room(
                id = "cocina",
                name = "Cocina",
                type = RoomType.KITCHEN,
                isFavorite = true,
                devices = listOf(
                    Device(
                        id = "cocina-oven",
                        name = "Horno",
                        type = DeviceType.OVEN,
                        roomId = "cocina",
                        isOn = true,
                    ),
                ),
            ),
            Room(
                id = "cuarto-principal",
                name = "Cuarto principal",
                type = RoomType.BEDROOM,
                devices = listOf(
                    Device("bed-light", "Lámpara de mesa", DeviceType.LIGHT, "cuarto-principal", isOn = true, brightness = 55),
                    Device("bed-ac", "Aire acond.", DeviceType.AC, "cuarto-principal", isOn = true, temperature = 21),
                    Device("bed-blinds", "Persiana", DeviceType.BLINDS, "cuarto-principal", isOn = true),
                    Device("bed-speaker", "Parlante", DeviceType.SPEAKER, "cuarto-principal", isOn = true, volume = 35),
                    Device("bed-alarm", "Alarma", DeviceType.ALARM, "cuarto-principal", isOn = false),
                ),
            ),
            Room(
                id = "bano-principal",
                name = "Baño principal",
                type = RoomType.BATHROOM,
            ),
            Room(
                id = "oficina",
                name = "Oficina",
                type = RoomType.OFFICE,
            ),
            Room(
                id = "comedor",
                name = "Comedor",
                type = RoomType.KITCHEN,
                devices = listOf(
                    Device("dining-light", "Luz de mesa", DeviceType.LIGHT, "comedor", isOn = true, brightness = 80),
                    Device("dining-speaker", "Parlante", DeviceType.SPEAKER, "comedor", isOn = true, volume = 40),
                    Device("dining-blinds", "Persiana", DeviceType.BLINDS, "comedor", isOn = false),
                    Device("dining-vacuum", "Aspiradora", DeviceType.VACUUM, "comedor", isOn = false),
                    Device("dining-alarm", "Alarma", DeviceType.ALARM, "comedor", isOn = false),
                ),
            ),
            Room(
                id = "cuarto-invitados",
                name = "Cuarto de invitados",
                type = RoomType.BEDROOM,
                devices = listOf(
                    Device("guest-light", "Lámpara", DeviceType.LIGHT, "cuarto-invitados", isOn = false, brightness = 0),
                    Device("guest-ac", "Aire acond.", DeviceType.AC, "cuarto-invitados", isOn = false, temperature = 24),
                ),
            ),
            Room(
                id = "bano-invitados",
                name = "Baño invitados",
                type = RoomType.BATHROOM,
            ),
        ),
        availableDeviceTypes = availableDeviceTypes,
    )

    val emptyHome = populated.copy(rooms = emptyList())

    val noResults = populated.copy(searchQuery = "terraza")

    val detailLiving = populated.copy(selectedRoomId = "living")

    val detailLivingEditingDevices = populated.copy(
        selectedRoomId = "living",
        editDevicesMode = true,
    )

    val detailBedroom = populated.copy(selectedRoomId = "cuarto-principal")

    val detailEmptyRoom = populated.copy(selectedRoomId = "bano-principal")
}
