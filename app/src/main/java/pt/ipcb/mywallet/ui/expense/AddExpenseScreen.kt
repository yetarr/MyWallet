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
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
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
import pt.ipcb.mywallet.ui.components.AppTextField
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

@Composable
fun AddExpenseScreen(
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    var amount by remember { mutableStateOf("63,00") }
    var selectedCategory by remember { mutableStateOf(0) }
    var selectedType by remember { mutableStateOf("single") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("29 de abril de 2026") }

    Column(modifier = Modifier.fillMaxSize().background(Neutral)) {
        TealBackHeader(title = "Nova despesa", onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(14.dp)
                .navigationBarsPadding()
                .imePadding(),
        ) {
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
                Text(text = "Valor da despesa", fontSize = 11.sp, color = TextHint)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "– ",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoralMid,
                        letterSpacing = (-0.5).sp,
                    )
                    BasicTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        textStyle = TextStyle(
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoralMid,
                            letterSpacing = (-0.5).sp,
                            textAlign = TextAlign.Center,
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    Text(
                        text = " €",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoralMid,
                        letterSpacing = (-0.5).sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            FieldLabel(text = "Categoria")
            Spacer(modifier = Modifier.height(6.dp))

            // Category grid — 4 columns
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                categories.chunked(4).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        rowItems.forEachIndexed { _, cat ->
                            val idx = categories.indexOf(cat)
                            CategoryPill(
                                category = cat,
                                selected = selectedCategory == idx,
                                onClick = { selectedCategory = idx },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            FieldLabel(text = "Tipo")
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                TypeButton(text = "Única", selected = selectedType == "single", onClick = { selectedType = "single" }, modifier = Modifier.weight(1f))
                TypeButton(text = "Recorrente", selected = selectedType == "recurring", onClick = { selectedType = "recurring" }, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            FieldLabel(text = "Descrição")
            Spacer(modifier = Modifier.height(6.dp))
            AppTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = "Pingo Doce — compras semanais",
            )

            Spacer(modifier = Modifier.height(12.dp))

            FieldLabel(text = "Data")
            Spacer(modifier = Modifier.height(6.dp))
            AppTextField(
                value = date,
                onValueChange = { date = it },
                placeholder = "DD de mês de AAAA",
            )

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
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(TealMid),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Localização detectada: Pingo Doce, Castelo Branco",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = null,
                        tint = TealDark,
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(modifier = Modifier.size(3.dp))
                    Text(text = "GPS", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TealDark)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(text = "Guardar despesa", onClick = onSaveClick)
        }
    }
}

@Composable
private fun CategoryPill(
    category: Category,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) TealLight else Color.White)
            .border(0.5.dp, if (selected) Color(0xFF5DCAA5) else NeutralMid, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(category.iconBgColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = TealDark,
                modifier = Modifier.size(15.dp),
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = category.label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) TealText else TextHint,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun TypeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) TealLight else Color.White)
            .border(0.5.dp, if (selected) Color(0xFF5DCAA5) else NeutralMid, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) TealText else TextHint,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddExpensePreview() {
    MyWalletTheme { AddExpenseScreen() }
}
