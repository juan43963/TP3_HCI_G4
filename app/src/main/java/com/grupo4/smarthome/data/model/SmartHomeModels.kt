package com.grupo4.smarthome.data.model

data class Home(
    val id: String,
    val name: String,
    val rooms: List<Room>,
    val type: HomeType = HomeType.OTRO,
    val photoIndex: Int? = null,
)

enum class HomeType {
    CASA,
    DEPARTAMENTO,
    OFICINA,
    OTRO,
}

data class SharedUser(
    val id: String,
    val email: String,
    val name: String,
)

data class Room(
    val id: String,
    val name: String,
    val type: RoomType,
    val isFavorite: Boolean = false,
    val devices: List<Device> = emptyList(),
) {
    val devicesTotal: Int get() = devices.size
    val devicesActive: Int get() = devices.count { it.isActive }
}

enum class RoomType {
    LIVING,
    BEDROOM,
    KITCHEN,
    BATHROOM,
    OFFICE,
    OTHER,
}

data class Device(
    val id: String,
    val name: String,
    val type: DeviceType,
    val roomId: String,
    val isOn: Boolean = false,
    val isFavorite: Boolean = false,
    val brightness: Int? = null,
    val temperature: Int? = null,
    val locked: Boolean? = null,
    val volume: Int? = null,
    val qrId: String? = null,
) {
    val isActive: Boolean
        get() = when (type) {
            DeviceType.DOOR -> locked == true
            else -> isOn
        }
}

enum class DeviceType {
    VACUUM,
    SPEAKER,
    FAUCET,
    BLINDS,
    LIGHT,
    OVEN,
    AC,
    DOOR,
    ALARM,
    FRIDGE,
}

// Acción que admite un tipo de dispositivo según el catálogo (GET /devicetypes); usada al crear rutinas
data class DeviceActionInfo(
    val name: String,
    val params: List<DeviceActionParamInfo> = emptyList(),
)

data class DeviceActionParamInfo(
    val name: String,
    val type: String,
    val min: Int? = null,
    val max: Int? = null,
)
