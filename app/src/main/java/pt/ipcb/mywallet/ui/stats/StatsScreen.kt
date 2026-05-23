package pt.ipcb.mywallet.ui.stats

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import pt.ipcb.mywallet.ui.components.BottomNavBar
import pt.ipcb.mywallet.ui.components.TealTitleHeader
import pt.ipcb.mywallet.ui.theme.Amber
import pt.ipcb.mywallet.ui.theme.CoralMid
import pt.ipcb.mywallet.ui.theme.MyWalletTheme
import pt.ipcb.mywallet.ui.theme.Neutral
import pt.ipcb.mywallet.ui.theme.NeutralMid
import pt.ipcb.mywallet.ui.theme.TealDark
import pt.ipcb.mywallet.ui.theme.TextHint
import pt.ipcb.mywallet.ui.theme.TextPrimary
import pt.ipcb.mywallet.ui.theme.TextSecondary
import pt.ipcb.mywallet.utils.Formatters
import pt.ipcb.mywallet.viewmodel.CategoryData
import pt.ipcb.mywallet.viewmodel.StatsViewModel

private val donutColors = listOf(TealDark, Amber, CoralMid, NeutralMid, TealDark, Amber, CoralMid, NeutralMid)

@Composable
fun StatsScreen(
    navController: NavController,
    onAddClick: () -> Unit = {},
    vm: StatsViewModel = viewModel(),
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) }

    val monthlyData by vm.monthlyExpenses.collectAsState()
    val categoryData by vm.categoryBreakdown.collectAsState()
    val income by vm.currentMonthIncome.collectAsState()
    val expenses by vm.currentMonthExpenses.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TealTitleHeader(title = "Estatísticas")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Neutral)
                .verticalScroll(rememberScrollState())
                .padding(14.dp)
                .navigationBarsPadding(),
        ) {
            TabNav(tabs = listOf("Mensal", "Categorias", "Tendência"), selectedIndex = selectedTab, onTabClick = { selectedTab = it })

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> {
                    ChartCard(title = "Despesas por mês") { BarChartView(monthlyData) }
                    Spacer(modifier = Modifier.height(10.dp))
                    TrendCardsRow(income = income, expenses = expenses)
                }
                1 -> {
                    ChartCard(title = "Por categoria") {
                        if (categoryData.isEmpty()) {
                            Text(text = "Sem despesas este mês.", fontSize = 12.sp, color = TextHint, modifier = Modifier.padding(vertical = 8.dp))
                        } else {
                            DonutChartRow(categoryData)
                        }
                    }
                }
                2 -> {
                    TrendCardsRow(income = income, expenses = expenses)
                }
            }
        }

        BottomNavBar(navController = navController, onAddClick = onAddClick)
    }
}

@Composable
private fun TabNav(tabs: List<String>, selectedIndex: Int, onTabClick: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Neutral)
            .border(0.5.dp, NeutralMid, RoundedCornerShape(10.dp))
            .padding(3.dp),
    ) {
        tabs.forEachIndexed { index, tab ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedIndex == index) Color.White else Color.Transparent)
                    .clickable { onTabClick(index) }
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = tab, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = if (selectedIndex == index) TealDark else TextHint)
            }
        }
    }
}

@Composable
private fun ChartCard(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(0.5.dp, NeutralMid, RoundedCornerShape(12.dp))
            .padding(14.dp),
    ) {
        Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun BarChartView(data: List<pt.ipcb.mywallet.viewmodel.BarData>) {
    if (data.isEmpty()) {
        Text(text = "Sem dados disponíveis.", fontSize = 12.sp, color = TextHint)
        return
    }
    val maxAmount = data.maxOfOrNull { it.amount } ?: 1.0
    val maxHeightDp = 76
    Row(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        data.forEachIndexed { idx, bar ->
            val isActive = idx == data.lastIndex
            val heightDp = if (maxAmount > 0) ((bar.amount / maxAmount) * maxHeightDp).toInt().coerceAtLeast(4) else 4
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(heightDp.dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(if (isActive) TealDark else NeutralMid),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = bar.label, fontSize = 9.sp, color = if (isActive) TealDark else TextHint, fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal)
            }
        }
    }
}

@Composable
private fun DonutChartRow(data: List<CategoryData>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Canvas(modifier = Modifier.size(72.dp)) {
            var startAngle = -90f
            data.forEachIndexed { idx, seg ->
                val sweep = 360f * seg.percentage
                drawArc(
                    color = donutColors[idx % donutColors.size],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = 16.dp.toPx()),
                )
                startAngle += sweep
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            data.forEachIndexed { idx, seg ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(donutColors[idx % donutColors.size]))
                    Spacer(modifier = Modifier.size(7.dp))
                    Text(text = seg.category, fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(1f))
                    Text(text = "${(seg.percentage * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun TrendCardsRow(income: Double, expenses: Double) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TrendCard(name = "Rendimentos do mês", value = Formatters.formatCurrency(income), isUp = true, modifier = Modifier.weight(1f))
        TrendCard(name = "Despesas do mês", value = Formatters.formatCurrency(expenses), isUp = false, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TrendCard(name: String, value: String, isUp: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(0.5.dp, NeutralMid, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(text = name, fontSize = 10.sp, color = TextHint)
        Spacer(modifier = Modifier.height(3.dp))
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = if (isUp) "▲ este mês" else "▼ este mês", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = if (isUp) TealDark else CoralMid)
    }
}

@Preview(showBackground = true)
@Composable
private fun StatsPreview() {
    MyWalletTheme { StatsScreen(navController = rememberNavController()) }
}
