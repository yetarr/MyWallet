package pt.ipcb.mywallet.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Dashboard : Screen("dashboard")
    data object AddExpense : Screen("add_expense")
    data object Stats : Screen("stats")
    data object Goals : Screen("goals")
    data object Family : Screen("family")
}
