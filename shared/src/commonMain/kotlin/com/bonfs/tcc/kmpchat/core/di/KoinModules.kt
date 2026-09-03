package com.bonfs.tcc.kmpchat.core.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

import com.bonfs.tcc.kmpchat.core.util.DefaultLogger
import com.bonfs.tcc.kmpchat.core.util.Logger
import com.bonfs.tcc.kmpchat.data.client.MatrixClientManager
import com.bonfs.tcc.kmpchat.data.local.DatabaseKeyProvider
import com.bonfs.tcc.kmpchat.data.local.RoomDatabaseFactory
import com.bonfs.tcc.kmpchat.data.repository.MatrixAuthRepository
import com.bonfs.tcc.kmpchat.data.repository.MatrixChatRepository
import com.bonfs.tcc.kmpchat.data.repository.MatrixRoomRepository
import com.bonfs.tcc.kmpchat.domain.repository.AuthRepository
import com.bonfs.tcc.kmpchat.domain.repository.ChatRepository
import com.bonfs.tcc.kmpchat.domain.repository.RoomRepository
import com.bonfs.tcc.kmpchat.domain.usecase.CreateDirectRoomUseCase
import com.bonfs.tcc.kmpchat.domain.usecase.HandleInviteUseCase
import com.bonfs.tcc.kmpchat.domain.usecase.LoginUseCase
import com.bonfs.tcc.kmpchat.domain.usecase.LogoutUseCase
import com.bonfs.tcc.kmpchat.domain.usecase.ObserveRoomsUseCase
import com.bonfs.tcc.kmpchat.domain.usecase.ObserveTimelineUseCase
import com.bonfs.tcc.kmpchat.domain.usecase.RestoreSessionUseCase
import com.bonfs.tcc.kmpchat.domain.usecase.SendMessageUseCase
import com.bonfs.tcc.kmpchat.ui.screens.chat.ChatViewModel
import com.bonfs.tcc.kmpchat.ui.screens.login.LoginViewModel
import com.bonfs.tcc.kmpchat.ui.screens.roomlist.RoomListViewModel

val coreModule: Module = module {
    single<Logger> { DefaultLogger() }
}

val domainModule: Module = module {
    factory { LoginUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { RestoreSessionUseCase(get()) }
    factory { ObserveRoomsUseCase(get()) }
    factory { CreateDirectRoomUseCase(get()) }
    factory { HandleInviteUseCase(get()) }
    factory { ObserveTimelineUseCase(get()) }
    factory { SendMessageUseCase(get()) }
}

val dataModule: Module = module {
    single { DatabaseKeyProvider() }
    single { RoomDatabaseFactory() }
    single { MatrixClientManager(get(), get()) }
    single<AuthRepository> { MatrixAuthRepository(get()) }
    single<RoomRepository> { MatrixRoomRepository(get()) }
    single<ChatRepository> { MatrixChatRepository(get()) }
}

val viewModelsModule: Module = module {
    factory { LoginViewModel(get(), get(), get()) }
    factory { RoomListViewModel(get(), get(), get(), get()) }
    factory { ChatViewModel(get(), get()) }
}

val appModules: List<Module> = listOf(
    coreModule,
    domainModule,
    dataModule,
    viewModelsModule
)

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(appModules)
    }
}
