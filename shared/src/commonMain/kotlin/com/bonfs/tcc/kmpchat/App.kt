package com.bonfs.tcc.kmpchat

import androidx.compose.runtime.Composable
import org.koin.compose.KoinContext

import com.bonfs.tcc.kmpchat.ui.navigation.AppNavHost
import com.bonfs.tcc.kmpchat.ui.theme.KmpChatTheme

@Composable
fun App() {
    KoinContext {
        KmpChatTheme {
            AppNavHost()
        }
    }
}