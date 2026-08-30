package com.bonfs.tcc.kmpchat.core.di

import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatformTools
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class KoinModuleTest {

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun shouldInitializeKoinSuccessfully() {
        initKoin()
        val koin = KoinPlatformTools.defaultContext().get()
        assertNotNull(koin)
    }
}
