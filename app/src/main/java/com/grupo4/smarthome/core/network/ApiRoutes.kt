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

    fun resolve(path: String): String =
        "${ApiConstants.BaseUrl.trimEnd('/')}/${path.trimStart('/')}"
}
