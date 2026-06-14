package com.grupo4.smarthome.data.model

data class Home(
    val id: String,
    val name: String,
    val rooms: List<Room>,
    val type: HomeType = HomeType.OTRO,
    val ownerName: String? = null,
    val ownerEmail: String? = null,
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
    val type: DeviceTypeInfo,
    val roomId: String,
    val state: DeviceState = DeviceState.Empty,
    val isFavorite: Boolean = false,
    val qrId: String? = null,
) {
    // Accessors de conveniencia derivados del estado dinámico: la UI lee estos, nunca el mapa crudo.
    val isOn: Boolean get() = state.isOn()
    val brightness: Int? get() = state.int("brightness")
    val temperature: Int? get() = state.int("temperature")
    val volume: Int? get() = state.int("volume")
    val locked: Boolean? get() = state.locked()

    val isActive: Boolean
        get() = if (type.isDoor) locked == true else isOn
}

/**
 * Tipo de dispositivo tal como lo describe el catálogo del back-end (`GET /devicetypes`). Reemplaza al
 * antiguo `enum DeviceType`: es la única fuente de tipos. Íconos/labels/controles se resuelven por
 * [canonicalName] (ver [DeviceTypeNames]); las acciones soportadas vienen en [actions].
 */
data class DeviceTypeInfo(
    val id: String,
    val name: String,
    val actions: List<DeviceActionInfo> = emptyList(),
    val powerUsage: Int? = null,
) {
    val canonicalName: String get() = DeviceTypeNames.canonical(name)
    val isDoor: Boolean get() = canonicalName == DeviceTypeNames.DOOR

    // Matching tolerante a mayúsculas (no asumimos que la API garantice el casing al consultar), pero
    // el `DeviceActionInfo.name` devuelto conserva el casing exacto del catálogo para ejecutar la acción.
    fun action(actionName: String): DeviceActionInfo? =
        actions.firstOrNull { it.name.equals(actionName, ignoreCase = true) }

    fun supports(actionName: String): Boolean = action(actionName) != null
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
