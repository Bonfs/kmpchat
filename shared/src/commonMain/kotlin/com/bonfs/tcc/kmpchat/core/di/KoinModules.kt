package com.bonfs.tcc.kmpchat.core.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

import com.bonfs.tcc.kmpchat.data.client.MatrixClientManager
import com.bonfs.tcc.kmpchat.data.local.DatabaseKeyProvider
import com.bonfs.tcc.kmpchat.data.local.RoomDatabaseFactory
import com.bonfs.tcc.kmpchat.data.repository.MatrixAuthRepositoryImpl
import com.bonfs.tcc.kmpchat.domain.repository.AuthRepository
import com.bonfs.tcc.kmpchat.domain.usecase.LoginUseCase
import com.bonfs.tcc.kmpchat.domain.usecase.LogoutUseCase

val appModule: Module = module {
}

val domainModule: Module = module {
    factory { LoginUseCase(get()) }
    factory { LogoutUseCase(get()) }
}

val dataModule: Module = module {
    single { DatabaseKeyProvider() }
    single { RoomDatabaseFactory() }
    single { MatrixClientManager(get(), get()) }
    single<AuthRepository> { MatrixAuthRepositoryImpl(get()) }
}

val viewModelsModule: Module = module {
}

val appModules: List<Module> = listOf(
    appModule,
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
