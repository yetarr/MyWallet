package pt.ipcb.mywallet

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.location.LocationServices
import pt.ipcb.mywallet.data.SessionManager
import pt.ipcb.mywallet.navigation.Screen
import pt.ipcb.mywallet.ui.auth.LoginScreen
import pt.ipcb.mywallet.ui.auth.RegisterScreen
import pt.ipcb.mywallet.ui.dashboard.DashboardScreen
import pt.ipcb.mywallet.ui.expense.AddExpenseScreen
import pt.ipcb.mywallet.ui.goals.GoalsScreen
import pt.ipcb.mywallet.ui.stats.StatsScreen
import pt.ipcb.mywallet.ui.theme.MyWalletTheme
import pt.ipcb.mywallet.ui.transactions.TransactionsScreen
import pt.ipcb.mywallet.viewmodel.DashboardViewModel

class MainActivity : ComponentActivity() {

    private val locationState = mutableStateOf<Location?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getLocation()
        enableEdgeToEdge()
        val startRoute = if (SessionManager(this).isLoggedIn()) Screen.Dashboard.route else Screen.Login.route
        setContent {
            MyWalletTheme {
                AppNavHost(
                    startDestination = startRoute,
                    location = locationState.value)
            }
        }
    }

    private fun getLocation() {
        val fusedClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
            return
        }

        fusedClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                locationState.value = location
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            getLocation()
        }
    }
}

@Composable
private fun AppNavHost(startDestination: String, location: Location?) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate(Screen.Register.route) },
                onForgotPasswordClick = {},
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onBackClick = { navController.popBackStack() },
                onRegisterSuccess = {
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
                onSeeAllClick = { navController.navigate(Screen.Transactions.route) },
            )
        }

        composable(
            route = "add_expense?id={id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType; defaultValue = -1 }),
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: -1
            AddExpenseScreen(
                transactionId = if (id == -1) null else id,
                onBackClick = { navController.popBackStack() },
                onSaveClick = { navController.popBackStack() },
                location = location
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

        composable(Screen.Transactions.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Dashboard.route)
            }
            val vm: DashboardViewModel = viewModel(parentEntry)
            TransactionsScreen(
                onBackClick = { navController.popBackStack() },
                onEditClick = { id -> navController.navigate(Screen.AddExpense.editRoute(id)) },
                vm = vm,
            )
        }
    }
}
