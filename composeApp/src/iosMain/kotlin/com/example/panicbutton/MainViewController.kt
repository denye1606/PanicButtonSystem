package com.example.panicbutton

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    val scope = rememberCoroutineScope()
    val platformActions = IosPlatformActions()
    val panicManager = PanicManager(scope, platformActions)
    
    App(panicManager)
}
