package pt.ipcb.mywallet.ui.family

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import pt.ipcb.mywallet.data.local.entity.FamilyMemberEntity
import pt.ipcb.mywallet.ui.components.AppTextField
import pt.ipcb.mywallet.ui.components.BottomNavBar
import pt.ipcb.mywallet.ui.components.FieldLabel
import pt.ipcb.mywallet.ui.components.SectionHeader
import pt.ipcb.mywallet.ui.components.StatCard
import pt.ipcb.mywallet.ui.components.TealTitleHeader
import pt.ipcb.mywallet.ui.theme.Amber
import pt.ipcb.mywallet.ui.theme.CoralMid
import pt.ipcb.mywallet.ui.theme.MyWalletTheme
import pt.ipcb.mywallet.ui.theme.Neutral
import pt.ipcb.mywallet.ui.theme.NeutralMid
import pt.ipcb.mywallet.ui.theme.TealDark
import pt.ipcb.mywallet.ui.theme.TealLight
import pt.ipcb.mywallet.ui.theme.TealText
import pt.ipcb.mywallet.ui.theme.TextHint
import pt.ipcb.mywallet.ui.theme.TextPrimary
import pt.ipcb.mywallet.utils.Formatters
import pt.ipcb.mywallet.viewmodel.FamilyViewModel

@Composable
fun FamilyScreen(
    navController: NavController,
    onAddClick: () -> Unit = {},
    vm: FamilyViewModel = viewModel(),
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    val members by vm.members.collectAsState()
    val totalIncome by vm.totalIncome.collectAsState()
    val totalExpenses by vm.totalExpenses.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TealTitleHeader(title = "Agregado familiar")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Neutral)
                .verticalScroll(rememberScrollState())
                .padding(14.dp)
                .navigationBarsPadding(),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(label = "Total rendimentos", value = Formatters.formatCurrency(totalIncome), isPositive = true, modifier = Modifier.weight(1f))
                StatCard(label = "Total despesas", value = Formatters.formatCurrency(totalExpenses), isPositive = false, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(14.dp))

            SectionHeader(title = "Membros", linkText = "+ Adicionar", onLinkClick = { showAddDialog = true })

            Spacer(modifier = Modifier.height(10.dp))

            if (members.isEmpty()) {
                Text(text = "Ainda não tens membros. Adiciona o primeiro!", fontSize = 12.sp, color = TextHint, modifier = Modifier.padding(vertical = 8.dp))
            } else {
                members.forEach { member -> MemberCard(member = member) }
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
                Text(text = "+ Adicionar membro", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TealText)
            }
        }

        BottomNavBar(navController = navController, onAddClick = onAddClick)
    }

    if (showAddDialog) {
        AddMemberDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { firstName, lastName, role, income, expenses, colorHex ->
                vm.addMember(firstName, lastName, role, income, expenses, colorHex)
                showAddDialog = false
            },
        )
    }
}

private fun memberAvatarColor(colorHex: String): Color = when (colorHex) {
    "amber" -> Amber
    "coral" -> CoralMid
    else -> TealDark
}

@Composable
private fun AddMemberDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, Double, Double, String) -> Unit) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var income by remember { mutableStateOf("") }
    var expenses by remember { mutableStateOf("") }
    var colorHex by remember { mutableStateOf("teal") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar membro", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        FieldLabel(text = "Nome")
                        AppTextField(value = firstName, onValueChange = { firstName = it }, placeholder = "Ana")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FieldLabel(text = "Apelido")
                        AppTextField(value = lastName, onValueChange = { lastName = it }, placeholder = "Silva")
                    }
                }
                FieldLabel(text = "Papel (ex: Cônjuge)")
                AppTextField(value = role, onValueChange = { role = it }, placeholder = "Cônjuge")
                FieldLabel(text = "Rendimento mensal (€)")
                AppTextField(value = income, onValueChange = { income = it }, placeholder = "0", keyboardType = KeyboardType.Decimal)
                FieldLabel(text = "Despesas mensais (€)")
                AppTextField(value = expenses, onValueChange = { expenses = it }, placeholder = "0", keyboardType = KeyboardType.Decimal)
                FieldLabel(text = "Cor do avatar")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("teal" to TealDark, "amber" to Amber, "coral" to CoralMid).forEach { (key, color) ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(2.dp, if (colorHex == key) Color.White else Color.Transparent, CircleShape)
                                .clickable { colorHex = key },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val inc = income.replace(",", ".").toDoubleOrNull() ?: 0.0
                    val exp = expenses.replace(",", ".").toDoubleOrNull() ?: 0.0
                    if (firstName.isNotBlank()) onConfirm(firstName, lastName, role, inc, exp, colorHex)
                },
            ) { Text("Guardar", color = TealDark) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        containerColor = Color.White,
    )
}

@Composable
private fun MemberCard(member: FamilyMemberEntity) {
    val initials = "${member.firstName.firstOrNull() ?: ""}${member.lastName.firstOrNull() ?: ""}".uppercase()
    val avatarColor = memberAvatarColor(member.colorHex)
    val balance = member.income - member.expenses

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(0.5.dp, NeutralMid, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 13.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier.size(38.dp).clip(CircleShape).background(avatarColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = initials, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
            Column {
                Text(text = "${member.firstName} ${member.lastName}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(text = member.role, fontSize = 10.sp, color = TextHint)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MiniStat(label = "Rendimento", value = Formatters.formatCurrency(member.income), isPositive = true, modifier = Modifier.weight(1f))
            MiniStat(label = "Despesas", value = Formatters.formatCurrency(member.expenses), isPositive = false, modifier = Modifier.weight(1f))
            MiniStat(label = "Saldo", value = Formatters.formatCurrency(balance), isPositive = balance >= 0, modifier = Modifier.weight(1f))
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun MiniStat(label: String, value: String, isPositive: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Neutral)
            .padding(horizontal = 8.dp, vertical = 7.dp),
    ) {
        Text(text = label, fontSize = 9.sp, color = TextHint)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (isPositive) TealDark else CoralMid)
    }
}

@Preview(showBackground = true)
@Composable
private fun FamilyPreview() {
    MyWalletTheme { FamilyScreen(navController = rememberNavController()) }
}
