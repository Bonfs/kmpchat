package com.bonfs.tcc.kmpchat

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

import com.bonfs.tcc.kmpchat.core.di.initKoin

fun MainViewController(): UIViewController {
    runCatching { initKoin() }
    return ComposeUIViewController { App() }
}