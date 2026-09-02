package com.bonfs.tcc.kmpchat.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.compose.koinInject

import com.bonfs.tcc.kmpchat.ui.screens.login.LoginScreen
import com.bonfs.tcc.kmpchat.ui.screens.login.LoginUiEffect
import com.bonfs.tcc.kmpchat.ui.screens.login.LoginViewModel

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = LoginRoute,
        modifier = modifier
    ) {
        composable<LoginRoute> {
            LoginDestination(
                onNavigateToMain = {
                    navController.navigate(MainRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                }
            )
        }
        composable<MainRoute> {
            AdaptiveMainLayout(
                onLogout = {
                    navController.navigate(LoginRoute) {
                        popUpTo(MainRoute) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
private fun LoginDestination(onNavigateToMain: () -> Unit) {
    val loginViewModel: LoginViewModel = koinInject()
    val state by loginViewModel.uiState.collectAsState()

    LaunchedEffect(loginViewModel) {
        loginViewModel.effect.collect { effect ->
            if (effect is LoginUiEffect.NavigateToRooms) {
                onNavigateToMain()
            }
        }
    }

    LoginScreen(
        state = state,
        onIntent = { loginViewModel.sendIntent(it) }
    )
}
