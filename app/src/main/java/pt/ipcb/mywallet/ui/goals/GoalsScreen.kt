package pt.ipcb.mywallet.ui.goals

import android.app.Activity
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import pt.ipcb.mywallet.data.local.entity.GoalEntity
import pt.ipcb.mywallet.ui.components.AppTextField
import pt.ipcb.mywallet.ui.components.BottomNavBar
import pt.ipcb.mywallet.ui.components.FieldLabel
import pt.ipcb.mywallet.ui.components.GoalProgressBar
import pt.ipcb.mywallet.ui.components.TealTitleHeader
import pt.ipcb.mywallet.ui.theme.Amber
import pt.ipcb.mywallet.ui.theme.AmberLight
import pt.ipcb.mywallet.ui.theme.AmberText
import pt.ipcb.mywallet.ui.theme.MyWalletTheme
import pt.ipcb.mywallet.ui.theme.Neutral
import pt.ipcb.mywallet.ui.theme.NeutralMid
import pt.ipcb.mywallet.ui.theme.TealDark
import pt.ipcb.mywallet.ui.theme.TealLight
import pt.ipcb.mywallet.ui.theme.TealText
import pt.ipcb.mywallet.ui.theme.TextHint
import pt.ipcb.mywallet.ui.theme.TextPrimary
import pt.ipcb.mywallet.ui.theme.TextSecondary
import pt.ipcb.mywallet.utils.Formatters
import pt.ipcb.mywallet.viewmodel.GoalsViewModel

@Composable
fun GoalsScreen(
    navController: NavController,
    onAddClick: () -> Unit = {},
    vm: GoalsViewModel = viewModel(),
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    val goals by vm.goals.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TealTitleHeader(title = "Objetivos financeiros")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Neutral)
                .verticalScroll(rememberScrollState())
                .padding(14.dp)
                .navigationBarsPadding(),
        ) {
            if (goals.isEmpty()) {
                Text(text = "Ainda não tens objetivos. Cria o primeiro!", fontSize = 12.sp, color = TextHint, modifier = Modifier.padding(vertical = 8.dp))
            } else {
                goals.forEach { goal ->
                    val progress = if (goal.targetAmount > 0) (goal.savedAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
                    val (icon, iconBg, iconTint) = goalIconTriple(goal.iconType)
                    val isAlmostDone = progress >= 0.7f
                    GoalCard(
                        icon = icon,
                        iconBgColor = iconBg,
                        iconTint = iconTint,
                        name = goal.name,
                        deadline = "Meta: ${Formatters.formatDate(goal.deadline)}",
                        percentage = progress,
                        badgeText = "${(progress * 100).toInt()}%",
                        badgeBgColor = if (isAlmostDone) AmberLight else TealLight,
                        badgeTextColor = if (isAlmostDone) AmberText else TealText,
                        barColor = if (isAlmostDone) Amber else TealDark,
                        saved = "${Formatters.formatCurrency(goal.savedAmount)} poupados",
                        remaining = "faltam ${Formatters.formatCurrency(goal.targetAmount - goal.savedAmount)}",
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TealLight)
                    .border(0.5.dp, Color(0xFF5DCAA5), RoundedCornerShape(12.dp))
                    .clickable { showAddDialog = true }
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "+ Novo objetivo", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TealText)
            }
        }

        BottomNavBar(navController = navController, onAddClick = onAddClick)
    }

    if (showAddDialog) {
        AddGoalDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, target, saved, iconType ->
                vm.addGoal(
                    name = name,
                    targetAmount = target,
                    savedAmount = saved,
                    deadline = System.currentTimeMillis() + 90L * 24 * 60 * 60 * 1000,
                    iconType = iconType,
                )
                showAddDialog = false
            },
        )
    }
}

private data class IconTriple(val icon: ImageVector, val bg: Color, val tint: Color)

private fun goalIconTriple(iconType: String): IconTriple = when (iconType) {
    "flight" -> IconTriple(Icons.Default.Flight, AmberLight, Amber)
    "computer" -> IconTriple(Icons.Default.Computer, TealLight, TealDark)
    "security" -> IconTriple(Icons.Default.Security, Neutral, TextSecondary)
    else -> IconTriple(Icons.Default.Flag, TealLight, TealDark)
}

@Composable
private fun AddGoalDialog(onDismiss: () -> Unit, onConfirm: (name: String, target: Double, saved: Double, iconType: String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf("0") }
    var iconType by remember { mutableStateOf("flag") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo objetivo", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FieldLabel(text = "Nome do objetivo")
                AppTextField(value = name, onValueChange = { name = it }, placeholder = "Ex: Férias de verão")
                FieldLabel(text = "Valor alvo (€)")
                AppTextField(value = target, onValueChange = { target = it }, placeholder = "1000", keyboardType = KeyboardType.Decimal)
                FieldLabel(text = "Valor já poupado (€)")
                AppTextField(value = saved, onValueChange = { saved = it }, placeholder = "0", keyboardType = KeyboardType.Decimal)
                FieldLabel(text = "Ícone")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("flag" to Icons.Default.Flag, "flight" to Icons.Default.Flight, "computer" to Icons.Default.Computer, "security" to Icons.Default.Security).forEach { (type, icon) ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (iconType == type) TealLight else Color.White)
                                .border(1.dp, if (iconType == type) TealDark else NeutralMid, RoundedCornerShape(8.dp))
                                .clickable { iconType = type },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(imageVector = icon, contentDescription = null, tint = if (iconType == type) TealDark else TextHint, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val t = target.replace(",", ".").toDoubleOrNull() ?: return@TextButton
                    val s = saved.replace(",", ".").toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && t > 0) onConfirm(name, t, s, iconType)
                },
            ) { Text("Guardar", color = TealDark) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        containerColor = Color.White,
    )
}

@Composable
private fun GoalCard(
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    name: String,
    deadline: String,
    percentage: Float,
    badgeText: String,
    badgeBgColor: Color,
    badgeTextColor: Color,
    barColor: Color,
    saved: String,
    remaining: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(0.5.dp, NeutralMid, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 13.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                Box(
                    modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(iconBgColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text(text = name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text(text = deadline, fontSize = 10.sp, color = TextHint)
                }
            }
            Box(
                modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(badgeBgColor).padding(horizontal = 9.dp, vertical = 3.dp),
            ) {
                Text(text = badgeText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = badgeTextColor)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        GoalProgressBar(progress = percentage, color = barColor)
        Spacer(modifier = Modifier.height(7.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = saved, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
            Text(text = remaining, fontSize = 11.sp, color = TextHint)
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Preview(showBackground = true)
@Composable
private fun GoalsPreview() {
    MyWalletTheme { GoalsScreen(navController = rememberNavController()) }
}
