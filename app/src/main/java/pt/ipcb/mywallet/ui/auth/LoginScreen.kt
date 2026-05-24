package pt.ipcb.mywallet.ui.auth

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ipcb.mywallet.ui.components.AppTextField
import pt.ipcb.mywallet.ui.components.FieldLabel
import pt.ipcb.mywallet.ui.components.PrimaryButton
import pt.ipcb.mywallet.ui.components.SecondaryButton
import pt.ipcb.mywallet.ui.theme.CoralMid
import pt.ipcb.mywallet.ui.theme.MyWalletTheme
import pt.ipcb.mywallet.ui.theme.NeutralMid
import pt.ipcb.mywallet.ui.theme.TealDark
import pt.ipcb.mywallet.ui.theme.TextHint
import pt.ipcb.mywallet.viewmodel.AuthState
import pt.ipcb.mywallet.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    vm: AuthViewModel = viewModel(),
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            vm.resetState()
            onLoginSuccess()
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TealDark),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("MyWallet", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(6.dp))
            Text("O teu gestor financeiro pessoal", fontSize = 13.sp, color = Color.White.copy(alpha = 0.6f), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    FieldLabel(text = "Email")
                    Spacer(modifier = Modifier.height(6.dp))
                    AppTextField(value = email, onValueChange = { email = it }, placeholder = "luis@email.com", keyboardType = KeyboardType.Email)

                    Spacer(modifier = Modifier.height(16.dp))
                    FieldLabel(text = "Palavra-passe")
                    Spacer(modifier = Modifier.height(6.dp))
                    AppTextField(value = password, onValueChange = { password = it }, placeholder = "••••••••", isPassword = true)

                    if (state is AuthState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = (state as AuthState.Error).message, fontSize = 12.sp, color = CoralMid)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    PrimaryButton(
                        text = if (state is AuthState.Loading) "A entrar..." else "Entrar",
                        onClick = { vm.login(email, password) },
                        enabled = state !is AuthState.Loading,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = NeutralMid, thickness = 0.5.dp)
                        Text(text = "ou", modifier = Modifier.padding(horizontal = 10.dp), fontSize = 11.sp, color = TextHint)
                        HorizontalDivider(modifier = Modifier.weight(1f), color = NeutralMid, thickness = 0.5.dp)
                    }

                    SecondaryButton(text = "Criar conta", onClick = onRegisterClick)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Esqueci a palavra-passe",
                        modifier = Modifier.fillMaxWidth().clickable { onForgotPasswordClick() },
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = TealDark,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    MyWalletTheme { LoginScreen() }
}
