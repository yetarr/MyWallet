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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import pt.ipcb.mywallet.ui.components.BottomNavBar
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
import pt.ipcb.mywallet.ui.theme.TextSecondary

@Composable
fun FamilyScreen(
    navController: NavController,
    onAddClick: () -> Unit = {},
    onAddMemberClick: () -> Unit = {},
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

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
            // Family totals
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatCard(label = "Total rendimentos", value = "3 200 €", isPositive = true, modifier = Modifier.weight(1f))
                StatCard(label = "Total despesas", value = "2 450 €", isPositive = false, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(14.dp))

            SectionHeader(title = "Membros", linkText = "+ Adicionar", onLinkClick = onAddMemberClick)

            Spacer(modifier = Modifier.height(10.dp))

            MemberCard(
                initials = "LS",
                avatarColor = TealDark,
                name = "Luís Silva",
                role = "Titular",
                income = "1 500 €",
                expenses = "1 200 €",
                balance = "+300 €",
                balancePositive = true,
            )

            MemberCard(
                initials = "AS",
                avatarColor = Amber,
                name = "Ana Silva",
                role = "Cônjuge",
                income = "1 700 €",
                expenses = "1 250 €",
                balance = "+450 €",
                balancePositive = true,
            )

            // Add member button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TealLight)
                    .border(0.5.dp, Color(0xFF5DCAA5), RoundedCornerShape(12.dp))
                    .clickable { onAddMemberClick() }
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "+ Adicionar membro",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TealText,
                )
            }
        }

        BottomNavBar(navController = navController, onAddClick = onAddClick)
    }
}

@Composable
private fun MemberCard(
    initials: String,
    avatarColor: Color,
    name: String,
    role: String,
    income: String,
    expenses: String,
    balance: String,
    balancePositive: Boolean,
) {
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
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = initials, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
            Column {
                Text(text = name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(text = role, fontSize = 10.sp, color = TextHint)
            }
        }

        // Stats row (3 columns)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            MiniStat(label = "Rendimento", value = income, isPositive = true, modifier = Modifier.weight(1f))
            MiniStat(label = "Despesas", value = expenses, isPositive = false, modifier = Modifier.weight(1f))
            MiniStat(label = "Saldo", value = balance, isPositive = balancePositive, modifier = Modifier.weight(1f))
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun MiniStat(
    label: String,
    value: String,
    isPositive: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Neutral)
            .padding(horizontal = 8.dp, vertical = 7.dp),
    ) {
        Text(text = label, fontSize = 9.sp, color = TextHint)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isPositive) TealDark else CoralMid,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FamilyPreview() {
    MyWalletTheme { FamilyScreen(navController = rememberNavController()) }
}
