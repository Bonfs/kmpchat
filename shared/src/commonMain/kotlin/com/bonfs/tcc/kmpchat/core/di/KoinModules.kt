package com.bonfs.tcc.kmpchat.core.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule: Module = module {
}

val domainModule: Module = module {
}

val dataModule: Module = module {
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
