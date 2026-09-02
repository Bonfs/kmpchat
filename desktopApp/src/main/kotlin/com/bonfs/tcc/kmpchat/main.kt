package com.bonfs.tcc.kmpchat

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

import com.bonfs.tcc.kmpchat.core.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "KmpChat Matrix E2EE",
            state = rememberWindowState(width = 960.dp, height = 680.dp)
        ) {
            App()
        }
    }
}