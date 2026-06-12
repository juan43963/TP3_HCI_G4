package com.grupo4.smarthome.core.di

import android.content.Context
import com.grupo4.smarthome.core.network.HttpClientProvider
import com.grupo4.smarthome.data.local.PreferencesDataStore
import com.grupo4.smarthome.data.local.SessionDataStore
import com.grupo4.smarthome.data.local.SessionManager
import com.grupo4.smarthome.data.remote.api.AuthApi
import com.grupo4.smarthome.data.remote.api.DevicesApi
import com.grupo4.smarthome.data.remote.api.HomesApi
import com.grupo4.smarthome.data.remote.api.ProfileApi
import com.grupo4.smarthome.data.remote.api.RoomsApi
import com.grupo4.smarthome.data.remote.api.RoutinesApi
import com.grupo4.smarthome.data.remote.socket.SmartHomeSocketClient
import com.grupo4.smarthome.data.remote.socket.SocketEvent
import com.grupo4.smarthome.data.repository.AuthRepository
import com.grupo4.smarthome.data.repository.AuthRepositoryImpl
import com.grupo4.smarthome.data.repository.DevicesRepository
import com.grupo4.smarthome.data.repository.DevicesRepositoryImpl
import com.grupo4.smarthome.data.repository.HomesRepository
import com.grupo4.smarthome.data.repository.HomesRepositoryImpl
import com.grupo4.smarthome.data.repository.LocalActionTracker
import com.grupo4.smarthome.data.repository.NotificationsRepository
import com.grupo4.smarthome.data.repository.NotificationsRepositoryImpl
import com.grupo4.smarthome.data.repository.ProfileRepository
import com.grupo4.smarthome.data.repository.ProfileRepositoryImpl
import com.grupo4.smarthome.data.repository.RoomsRepository
import com.grupo4.smarthome.data.repository.RoomsRepositoryImpl
import com.grupo4.smarthome.data.repository.RoutinesRepository
import com.grupo4.smarthome.data.repository.RoutinesRepositoryImpl
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn

class AppContainer(context: Context) {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val sessionDataStore: SessionDataStore = SessionDataStore(context.applicationContext)
    val sessionManager: SessionManager = SessionManager(sessionDataStore, applicationScope)

    val preferencesDataStore: PreferencesDataStore = PreferencesDataStore(context.applicationContext)

    val httpClient: HttpClient = HttpClientProvider.create { sessionManager.currentToken() }

    private val authApi: AuthApi = AuthApi(httpClient)

    val authRepository: AuthRepository = AuthRepositoryImpl(authApi, sessionManager)

    private val profileApi: ProfileApi = ProfileApi(httpClient)

    val profileRepository: ProfileRepository = ProfileRepositoryImpl(profileApi, sessionManager)

    private val homesApi: HomesApi = HomesApi(httpClient)

    val homesRepository: HomesRepository = HomesRepositoryImpl(homesApi)

    private val roomsApi: RoomsApi = RoomsApi(httpClient)

    val roomsRepository: RoomsRepository = RoomsRepositoryImpl(roomsApi)

    private val devicesApi: DevicesApi = DevicesApi(httpClient)

    private val localActionTracker: LocalActionTracker = LocalActionTracker()

    val devicesRepository: DevicesRepository = DevicesRepositoryImpl(devicesApi, localActionTracker)

    private val routinesApi: RoutinesApi = RoutinesApi(httpClient)

    val routinesRepository: RoutinesRepository = RoutinesRepositoryImpl(routinesApi)

    private val socketClient: SmartHomeSocketClient =
        SmartHomeSocketClient { sessionManager.currentToken() }

    val socketEvents: SharedFlow<SocketEvent> =
        socketClient.events().shareIn(applicationScope, SharingStarted.WhileSubscribed(5_000L))

    val notificationsRepository: NotificationsRepository =
        NotificationsRepositoryImpl(devicesRepository, socketEvents, localActionTracker)
}
