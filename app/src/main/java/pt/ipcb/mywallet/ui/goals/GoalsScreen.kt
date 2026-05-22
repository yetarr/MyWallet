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
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import pt.ipcb.mywallet.ui.components.BottomNavBar
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

@Composable
fun GoalsScreen(
    navController: NavController,
    onAddClick: () -> Unit = {},
    onAddGoalClick: () -> Unit = {},
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

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
            GoalCard(
                icon = Icons.Default.Flight,
                iconBgColor = AmberLight,
                iconTint = Amber,
                name = "Férias de verão",
                deadline = "Meta: julho 2026",
                percentage = 0.62f,
                badgeText = "62%",
                badgeBgColor = AmberLight,
                badgeTextColor = AmberText,
                barColor = Amber,
                saved = "620 € poupados",
                remaining = "faltam 380 €",
            )

            GoalCard(
                icon = Icons.Default.Computer,
                iconBgColor = TealLight,
                iconTint = TealDark,
                name = "Portátil novo",
                deadline = "Meta: outubro 2026",
                percentage = 0.31f,
                badgeText = "31%",
                badgeBgColor = TealLight,
                badgeTextColor = TealText,
                barColor = TealDark,
                saved = "310 € poupados",
                remaining = "faltam 690 €",
            )

            GoalCard(
                icon = Icons.Default.Security,
                iconBgColor = Neutral,
                iconTint = TextSecondary,
                name = "Fundo de emergência",
                deadline = "Meta: 3 meses despesas",
                percentage = 0.80f,
                badgeText = "80%",
                badgeBgColor = AmberLight,
                badgeTextColor = AmberText,
                barColor = Amber,
                saved = "2 880 € poupados",
                remaining = "faltam 720 €",
            )

            // Add goal button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TealLight)
                    .border(0.5.dp, Color(0xFF5DCAA5), RoundedCornerShape(12.dp))
                    .clickable { onAddGoalClick() }
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "+ Novo objetivo",
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Column {
                    Text(text = name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text(text = deadline, fontSize = 10.sp, color = TextHint)
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(badgeBgColor)
                    .padding(horizontal = 9.dp, vertical = 3.dp),
            ) {
                Text(text = badgeText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = badgeTextColor)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        GoalProgressBar(progress = percentage, color = barColor)

        Spacer(modifier = Modifier.height(7.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
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
