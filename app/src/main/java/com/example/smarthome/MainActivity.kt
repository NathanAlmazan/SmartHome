package com.example.smarthome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.smarthome.ui.screens.AppScaffold
import com.example.smarthome.ui.theme.SmartHomeTheme
import com.example.smarthome.ui.viewmodels.MainViewModel

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels { MainViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartHomeTheme {
                AppScaffold(mainViewModel)
            }
        }
    }
}