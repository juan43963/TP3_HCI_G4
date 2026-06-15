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

    fun homeRooms(homeId: String): String = "$Homes/$homeId/rooms"

    fun roomById(roomId: String): String = "$Rooms/$roomId"

    fun homeRoomAssociation(homeId: String, roomId: String): String = "$Homes/$homeId/rooms/$roomId"

    fun homeRoomRemoval(roomId: String): String = "$Homes/rooms/$roomId"

    fun roomDevices(roomId: String): String = "$Rooms/$roomId/devices"

    fun deviceById(deviceId: String): String = "$Devices/$deviceId"

    fun deviceState(deviceId: String): String = "$Devices/$deviceId/state"

    fun deviceLogs(limit: Int, offset: Int): String = "$Devices/logs/limit/$limit/offset/$offset"

    fun deviceAction(deviceId: String, actionName: String): String = "$Devices/$deviceId/$actionName"

    fun deviceTypeById(deviceTypeId: String): String = "$DeviceTypes/$deviceTypeId"

    fun roomDeviceAssociation(roomId: String, deviceId: String): String = "$Rooms/$roomId/devices/$deviceId"

    fun roomDeviceRemoval(deviceId: String): String = "$Rooms/devices/$deviceId"

    fun routineById(routineId: String): String = "$Routines/$routineId"

    fun routineExecute(routineId: String): String = "$Routines/$routineId/execute"

    fun resolve(path: String): String =
        "${ApiConstants.BaseUrl.trimEnd('/')}/${path.trimStart('/')}"
}
