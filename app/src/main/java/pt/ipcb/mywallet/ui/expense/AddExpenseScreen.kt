package pt.ipcb.mywallet.ui.expense

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ipcb.mywallet.ui.components.FieldLabel
import pt.ipcb.mywallet.ui.components.PrimaryButton
import pt.ipcb.mywallet.ui.components.TealBackHeader
import pt.ipcb.mywallet.ui.theme.AmberLight
import pt.ipcb.mywallet.ui.theme.CoralLight
import pt.ipcb.mywallet.ui.theme.CoralMid
import pt.ipcb.mywallet.ui.theme.MyWalletTheme
import pt.ipcb.mywallet.ui.theme.Neutral
import pt.ipcb.mywallet.ui.theme.NeutralMid
import pt.ipcb.mywallet.ui.theme.TealDark
import pt.ipcb.mywallet.ui.theme.TealLight
import pt.ipcb.mywallet.ui.theme.TealMid
import pt.ipcb.mywallet.ui.theme.TealText
import pt.ipcb.mywallet.ui.theme.TextHint
import pt.ipcb.mywallet.ui.theme.TextSecondary
import pt.ipcb.mywallet.utils.Formatters
import pt.ipcb.mywallet.viewmodel.AddExpenseViewModel
import pt.ipcb.mywallet.ui.components.AppTextField

private data class Category(val icon: ImageVector, val iconBgColor: Color, val label: String)

private val categories = listOf(
    Category(Icons.Default.ShoppingCart, TealLight, "Alimentação"),
    Category(Icons.Default.DirectionsCar, AmberLight, "Transporte"),
    Category(Icons.Default.SportsEsports, CoralLight, "Lazer"),
    Category(Icons.Default.Home, Neutral, "Casa"),
    Category(Icons.Default.MedicalServices, TealLight, "Saúde"),
    Category(Icons.Default.Subscriptions, AmberLight, "Subscr."),
    Category(Icons.Default.ShoppingBag, CoralLight, "Compras"),
    Category(Icons.Default.Category, Neutral, "Outros"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    vm: AddExpenseViewModel = viewModel(),
) {
    val saved by vm.saved.collectAsState()
    LaunchedEffect(saved) {
        if (saved) { vm.resetSaved(); onSaveClick() }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    var isExpense by remember { mutableStateOf(true) }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(0) }
    var isRecurring by remember { mutableStateOf(false) }
    var recurEndDate by remember { mutableStateOf<Long?>(null) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    val dateStr = Formatters.formatDate(System.currentTimeMillis())

    Column(modifier = Modifier.fillMaxSize().background(Neutral)) {
        TealBackHeader(title = if (isExpense) "Nova despesa" else "Novo rendimento", onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(14.dp)
                .navigationBarsPadding()
                .imePadding(),
        ) {
            // Income / Expense toggle
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                TypeButton(text = "Despesa", selected = isExpense, onClick = { isExpense = true }, modifier = Modifier.weight(1f))
                TypeButton(text = "Rendimento", selected = !isExpense, onClick = { isExpense = false }, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Amount display
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(0.5.dp, NeutralMid, RoundedCornerShape(14.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = if (isExpense) "Valor da despesa" else "Valor do rendimento", fontSize = 11.sp, color = TextHint)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isExpense) "– " else "+ ",
                        fontSize = 34.sp, fontWeight = FontWeight.Bold,
                        color = if (isExpense) CoralMid else TealDark,
                        letterSpacing = (-0.5).sp,
                    )
                    BasicTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        textStyle = TextStyle(
                            fontSize = 34.sp, fontWeight = FontWeight.Bold,
                            color = if (isExpense) CoralMid else TealDark,
                            letterSpacing = (-0.5).sp, textAlign = TextAlign.Center,
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        decorationBox = { inner ->
                            if (amount.isEmpty()) {
                                Text("0,00", fontSize = 34.sp, fontWeight = FontWeight.Bold,
                                    color = if (isExpense) CoralMid.copy(alpha = 0.3f) else TealDark.copy(alpha = 0.3f),
                                    letterSpacing = (-0.5).sp)
                            }
                            inner()
                        },
                    )
                    Text(text = " €", fontSize = 34.sp, fontWeight = FontWeight.Bold,
                        color = if (isExpense) CoralMid else TealDark, letterSpacing = (-0.5).sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            FieldLabel(text = "Categoria")
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                categories.chunked(4).forEach { rowItems ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        rowItems.forEach { cat ->
                            val idx = categories.indexOf(cat)
                            CategoryPill(category = cat, selected = selectedCategory == idx, onClick = { selectedCategory = idx }, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            FieldLabel(text = "Tipo")
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                TypeButton(text = "Única", selected = !isRecurring, onClick = { isRecurring = false; recurEndDate = null }, modifier = Modifier.weight(1f))
                TypeButton(text = "Recorrente", selected = isRecurring, onClick = { isRecurring = true }, modifier = Modifier.weight(1f))
            }

            // End date picker — only shown for recurring
            if (isRecurring) {
                Spacer(modifier = Modifier.height(10.dp))
                FieldLabel(text = "Data de fim (opcional)")
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .border(0.5.dp, NeutralMid, RoundedCornerShape(10.dp))
                        .clickable { showEndDatePicker = true }
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = TealDark, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = if (recurEndDate != null) Formatters.formatDate(recurEndDate!!) else "Sem data de fim",
                        fontSize = 13.sp,
                        color = if (recurEndDate != null) TextSecondary else TextHint,
                        modifier = Modifier.weight(1f),
                    )
                    if (recurEndDate != null) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Limpar",
                            tint = TextHint,
                            modifier = Modifier.size(14.dp).clickable { recurEndDate = null },
                        )
                    } else {
                        Text(text = "Selecionar", fontSize = 11.sp, color = TealDark, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            FieldLabel(text = "Descrição")
            Spacer(modifier = Modifier.height(6.dp))
            AppTextField(value = description, onValueChange = { description = it }, placeholder = "Pingo Doce — compras semanais")

            Spacer(modifier = Modifier.height(12.dp))

            FieldLabel(text = "Data")
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Neutral)
                    .border(0.5.dp, NeutralMid, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 11.dp),
            ) {
                Text(text = dateStr, fontSize = 13.sp, color = TextSecondary)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // GPS row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .border(0.5.dp, NeutralMid, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(TealMid))
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = "Localização detectada automaticamente", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.size(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.GpsFixed, contentDescription = null, tint = TealDark, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.size(3.dp))
                    Text(text = "GPS", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TealDark)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                text = if (isExpense) "Guardar despesa" else "Guardar rendimento",
                onClick = {
                    vm.save(
                        name = description,
                        amountStr = amount,
                        isExpense = isExpense,
                        category = categories[selectedCategory].label,
                        isRecurring = isRecurring,
                        endDate = recurEndDate,
                        description = description,
                        date = System.currentTimeMillis(),
                        locationName = null,
                    )
                },
                enabled = amount.isNotBlank(),
            )
        }
    }

    // ── End date picker dialog ─────────────────────────────────────────────────
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = recurEndDate ?: System.currentTimeMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { recurEndDate = it }
                    showEndDatePicker = false
                }) { Text("OK", color = TealDark) }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancelar") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun CategoryPill(category: Category, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) TealLight else Color.White)
            .border(0.5.dp, if (selected) Color(0xFF5DCAA5) else NeutralMid, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(category.iconBgColor), contentAlignment = Alignment.Center) {
            Icon(imageVector = category.icon, contentDescription = null, tint = TealDark, modifier = Modifier.size(15.dp))
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(text = category.label, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = if (selected) TealText else TextHint, textAlign = TextAlign.Center, maxLines = 1)
    }
}

@Composable
private fun TypeButton(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) TealLight else Color.White)
            .border(0.5.dp, if (selected) Color(0xFF5DCAA5) else NeutralMid, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = if (selected) TealText else TextHint)
    }
}

@Preview(showBackground = true)
@Composable
private fun AddExpensePreview() {
    MyWalletTheme { AddExpenseScreen() }
}
