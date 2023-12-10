package com.example.smarthome.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smarthome.ui.viewmodels.MainViewModel

sealed class Screen(val route: String) {
    data object Home : Screen("homeScreen")
    data object Energy : Screen("energyScreen")
    data object Device : Screen("deviceScreen")
}

@Composable
fun AppScaffold(mainViewModel: MainViewModel) {
    val navController = rememberNavController()

    Scaffold {
        AppNavHost(modifier = Modifier.padding(it), navController, mainViewModel)
    }
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    mainViewModel: MainViewModel,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            HomeScreen(mainViewModel) { navController.navigate(it.route) }
        }

        composable(Screen.Energy.route) {
            EnergyScreen(mainViewModel) { navController.navigate(it.route) }
        }

        composable(Screen.Device.route) {
            DeviceScreen(mainViewModel) { navController.navigate(it.route) }
        }
    }
}