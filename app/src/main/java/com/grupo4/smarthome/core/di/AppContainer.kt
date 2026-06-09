package com.grupo4.smarthome.core.di

import android.content.Context
import com.grupo4.smarthome.core.network.HttpClientProvider
import com.grupo4.smarthome.data.local.PreferencesDataStore
import com.grupo4.smarthome.data.local.SessionDataStore
import com.grupo4.smarthome.data.local.SessionManager
import com.grupo4.smarthome.data.remote.api.AuthApi
import com.grupo4.smarthome.data.remote.api.HomesApi
import com.grupo4.smarthome.data.remote.api.RoomsApi
import com.grupo4.smarthome.data.repository.AuthRepository
import com.grupo4.smarthome.data.repository.AuthRepositoryImpl
import com.grupo4.smarthome.data.repository.HomesRepository
import com.grupo4.smarthome.data.repository.HomesRepositoryImpl
import com.grupo4.smarthome.data.repository.RoomsRepository
import com.grupo4.smarthome.data.repository.RoomsRepositoryImpl
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(context: Context) {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val sessionDataStore: SessionDataStore = SessionDataStore(context.applicationContext)
    val sessionManager: SessionManager = SessionManager(sessionDataStore, applicationScope)

    val preferencesDataStore: PreferencesDataStore = PreferencesDataStore(context.applicationContext)

    val httpClient: HttpClient = HttpClientProvider.create { sessionManager.currentToken() }

    private val authApi: AuthApi = AuthApi(httpClient)

    val authRepository: AuthRepository = AuthRepositoryImpl(authApi, sessionManager)

    private val homesApi: HomesApi = HomesApi(httpClient)

    val homesRepository: HomesRepository = HomesRepositoryImpl(homesApi)

    private val roomsApi: RoomsApi = RoomsApi(httpClient)

    val roomsRepository: RoomsRepository = RoomsRepositoryImpl(roomsApi)
}
