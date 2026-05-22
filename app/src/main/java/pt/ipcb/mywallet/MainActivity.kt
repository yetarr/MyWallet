package pt.ipcb.mywallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pt.ipcb.mywallet.navigation.Screen
import pt.ipcb.mywallet.ui.auth.LoginScreen
import pt.ipcb.mywallet.ui.auth.RegisterScreen
import pt.ipcb.mywallet.ui.dashboard.DashboardScreen
import pt.ipcb.mywallet.ui.expense.AddExpenseScreen
import pt.ipcb.mywallet.ui.family.FamilyScreen
import pt.ipcb.mywallet.ui.goals.GoalsScreen
import pt.ipcb.mywallet.ui.stats.StatsScreen
import pt.ipcb.mywallet.ui.theme.MyWalletTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyWalletTheme {
                AppNavHost()
            }
        }
    }
}

@Composable
private fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginClick = { _, _ ->
                    // TODO: authenticate with Firebase Auth, then navigate on success
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate(Screen.Register.route) },
                onForgotPasswordClick = { /* TODO: password reset */ },
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onBackClick = { navController.popBackStack() },
                onRegisterClick = {
                    // TODO: create account with Firebase Auth, then navigate
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                navController = navController,
                onAddClick = { navController.navigate(Screen.AddExpense.route) },
            )
        }

        composable(Screen.AddExpense.route) {
            AddExpenseScreen(
                onBackClick = { navController.popBackStack() },
                onSaveClick = { navController.popBackStack() },
            )
        }

        composable(Screen.Stats.route) {
            StatsScreen(
                navController = navController,
                onAddClick = { navController.navigate(Screen.AddExpense.route) },
            )
        }

        composable(Screen.Goals.route) {
            GoalsScreen(
                navController = navController,
                onAddClick = { navController.navigate(Screen.AddExpense.route) },
            )
        }

        composable(Screen.Family.route) {
            FamilyScreen(
                navController = navController,
                onAddClick = { navController.navigate(Screen.AddExpense.route) },
            )
        }
    }
}
