package com.automate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.automate.app.engine.ActionRunner
import com.automate.app.ui.AutoMateRoot
import com.automate.app.ui.theme.AutoMateTheme
import com.automate.app.ui.theme.LocalAnimationsOn

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: AppViewModel = viewModel()
            AutoMateTheme(vm.theme) {
                CompositionLocalProvider(LocalAnimationsOn provides vm.animations) {
                    AutoMateRoot(vm)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        ActionRunner.appVisible = true
    }

    override fun onStop() {
        super.onStop()
        ActionRunner.appVisible = false
    }
}
