package com.grupo4.smarthome.core.network

object ApiRoutes {
    const val UsersRegister = "users/register"
    const val UsersLogin = "users/login"
    const val UsersProfile = "users/profile"
    const val UsersVerifyAccount = "users/verify-account"
    const val UsersSendVerification = "users/send-verification"
    const val UsersForgotPassword = "users/forgot-password"
    const val UsersResetPassword = "users/reset-password"
    const val UsersChangePassword = "users/change-password"
    const val UsersLogout = "users/logout"

    const val Homes = "homes"

    const val Rooms = "rooms"

    const val Devices = "devices"

    const val DeviceTypes = "devicetypes"

    const val Routines = "routines"

    fun homeById(homeId: String): String = "$Homes/$homeId"

    fun homeShare(homeId: String): String = "$Homes/$homeId/share"

    // GET de las habitaciones de un hogar
    fun homeRooms(homeId: String): String = "$Homes/$homeId/rooms"

    // GET/PUT/DELETE de una habitación puntual
    fun roomById(roomId: String): String = "$Rooms/$roomId"

    // POST para asociar una habitación existente a un hogar
    fun homeRoomAssociation(homeId: String, roomId: String): String = "$Homes/$homeId/rooms/$roomId"

    // DELETE para desasociar una habitación de su hogar
    fun homeRoomRemoval(roomId: String): String = "$Homes/rooms/$roomId"

    // GET de los dispositivos de una habitación
    fun roomDevices(roomId: String): String = "$Rooms/$roomId/devices"

    // GET/PUT/DELETE de un dispositivo puntual
    fun deviceById(deviceId: String): String = "$Devices/$deviceId"

    // GET del estado actual de un dispositivo
    fun deviceState(deviceId: String): String = "$Devices/$deviceId/state"

    // PATCH para ejecutar una acción (turnOn, setBrightness, etc.) sobre un dispositivo
    fun deviceAction(deviceId: String, actionName: String): String = "$Devices/$deviceId/$actionName"

    // GET de un tipo de dispositivo puntual del catálogo
    fun deviceTypeById(deviceTypeId: String): String = "$DeviceTypes/$deviceTypeId"

    // POST para asociar un dispositivo existente a una habitación (fallback de POST /devices)
    fun roomDeviceAssociation(roomId: String, deviceId: String): String = "$Rooms/$roomId/devices/$deviceId"

    // DELETE para desvincular un dispositivo de su habitación
    fun roomDeviceRemoval(deviceId: String): String = "$Rooms/devices/$deviceId"

    // GET/PUT/DELETE de una rutina puntual
    fun routineById(routineId: String): String = "$Routines/$routineId"

    // PATCH para ejecutar todas las acciones de una rutina
    fun routineExecute(routineId: String): String = "$Routines/$routineId/execute"

    fun resolve(path: String): String =
        "${ApiConstants.BaseUrl.trimEnd('/')}/${path.trimStart('/')}"
}
