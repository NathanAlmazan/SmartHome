package com.example.smarthome.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smarthome.ui.viewmodels.MainViewModel
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    data object Home : Screen("homeScreen")
    data object Energy : Screen("energyScreen")
    data object Device : Screen("deviceScreen")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppScaffold(mainViewModel: MainViewModel) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(mainViewModel.error) {
        mainViewModel.error?.let { error ->
            scope.launch {
                snackBarHostState.showSnackbar(error)
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    ) {
        AppNavHost(modifier = Modifier.padding(it), navController, mainViewModel)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
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