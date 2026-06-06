package com.grupo4.smarthome.ui.screens.rooms.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.grupo4.smarthome.R
import com.grupo4.smarthome.data.model.DeviceType
import com.grupo4.smarthome.data.model.RoomType
import com.grupo4.smarthome.ui.theme.SmartHomeIcons

@Composable
fun roomTypeLabel(type: RoomType): String = stringResource(
    when (type) {
        RoomType.LIVING -> R.string.rooms_type_living
        RoomType.BEDROOM -> R.string.rooms_type_bedroom
        RoomType.KITCHEN -> R.string.rooms_type_kitchen
        RoomType.BATHROOM -> R.string.rooms_type_bathroom
        RoomType.OFFICE -> R.string.rooms_type_office
        RoomType.OTHER -> R.string.rooms_type_other
    },
)

fun roomTypeIcon(type: RoomType): ImageVector = when (type) {
    RoomType.LIVING -> SmartHomeIcons.Living
    RoomType.BEDROOM -> SmartHomeIcons.Bedroom
    RoomType.KITCHEN -> SmartHomeIcons.Kitchen
    RoomType.BATHROOM -> SmartHomeIcons.Bathroom
    RoomType.OFFICE -> SmartHomeIcons.Office
    RoomType.OTHER -> SmartHomeIcons.Rooms
}

@Composable
fun deviceTypeLabel(type: DeviceType): String = stringResource(
    when (type) {
        DeviceType.VACUUM -> R.string.rooms_device_type_vacuum
        DeviceType.SPEAKER -> R.string.rooms_device_type_speaker
        DeviceType.FAUCET -> R.string.rooms_device_type_faucet
        DeviceType.BLINDS -> R.string.rooms_device_type_blinds
        DeviceType.LIGHT -> R.string.rooms_device_type_light
        DeviceType.OVEN -> R.string.rooms_device_type_oven
        DeviceType.AC -> R.string.rooms_device_type_ac
        DeviceType.DOOR -> R.string.rooms_device_type_door
        DeviceType.ALARM -> R.string.rooms_device_type_alarm
        DeviceType.FRIDGE -> R.string.rooms_device_type_fridge
    },
)

fun deviceTypeIcon(type: DeviceType): ImageVector = when (type) {
    DeviceType.VACUUM -> SmartHomeIcons.Vacuum
    DeviceType.SPEAKER -> SmartHomeIcons.Speaker
    DeviceType.FAUCET -> SmartHomeIcons.Faucet
    DeviceType.BLINDS -> SmartHomeIcons.Blinds
    DeviceType.LIGHT -> SmartHomeIcons.Light
    DeviceType.OVEN -> SmartHomeIcons.Oven
    DeviceType.AC -> SmartHomeIcons.AirConditioner
    DeviceType.DOOR -> SmartHomeIcons.Door
    DeviceType.ALARM -> SmartHomeIcons.Alarm
    DeviceType.FRIDGE -> SmartHomeIcons.Fridge
}
