package pt.ipcb.mywallet.ui.auth

import android.app.Activity
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ipcb.mywallet.ui.components.AppTextField
import pt.ipcb.mywallet.ui.components.FieldLabel
import pt.ipcb.mywallet.ui.components.PrimaryButton
import pt.ipcb.mywallet.ui.components.TealBackHeader
import pt.ipcb.mywallet.ui.theme.CoralMid
import pt.ipcb.mywallet.ui.theme.MyWalletTheme
import pt.ipcb.mywallet.ui.theme.Neutral
import pt.ipcb.mywallet.ui.theme.NeutralMid
import pt.ipcb.mywallet.ui.theme.TealDark
import pt.ipcb.mywallet.ui.theme.TealLight
import pt.ipcb.mywallet.ui.theme.TealText
import pt.ipcb.mywallet.ui.theme.TextHint
import pt.ipcb.mywallet.ui.theme.TextSecondary
import pt.ipcb.mywallet.viewmodel.AuthState
import pt.ipcb.mywallet.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    onBackClick: () -> Unit = {},
    onRegisterSuccess: () -> Unit = {},
    vm: AuthViewModel = viewModel(),
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            vm.resetState()
            onRegisterSuccess()
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    val context = LocalContext.current
    var profilePhotoUri by remember { mutableStateOf<Uri?>(null) }
    var profilePhotoBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        profilePhotoUri = uri
    }
    LaunchedEffect(profilePhotoUri) {
        profilePhotoBitmap = profilePhotoUri?.let { uri ->
            withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
                }.getOrNull()
            }
        }
    }

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("EUR") }
    var termsAccepted by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(Neutral)) {
        TealBackHeader(title = "Criar conta", onBackClick = onBackClick, titleFontSize = 16, horizontalPadding = 20)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .navigationBarsPadding()
                .imePadding(),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(TealLight)
                    .border(BorderStroke(1.5.dp, Color(0xFF5DCAA5)), CircleShape)
                    .align(Alignment.CenterHorizontally)
                    .clickable { photoLauncher.launch("image/*") },
                contentAlignment = Alignment.Center,
            ) {
                if (profilePhotoBitmap != null) {
                    Image(
                        bitmap = profilePhotoBitmap!!,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = null, tint = TealText, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    FieldLabel(text = "Nome")
                    Spacer(modifier = Modifier.height(6.dp))
                    AppTextField(value = firstName, onValueChange = { firstName = it }, placeholder = "Luís")
                }
                Column(modifier = Modifier.weight(1f)) {
                    FieldLabel(text = "Apelido")
                    Spacer(modifier = Modifier.height(6.dp))
                    AppTextField(value = lastName, onValueChange = { lastName = it }, placeholder = "Silva")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            FieldLabel(text = "Email")
            Spacer(modifier = Modifier.height(6.dp))
            AppTextField(value = email, onValueChange = { email = it }, placeholder = "luis@email.com", keyboardType = KeyboardType.Email)

            Spacer(modifier = Modifier.height(10.dp))
            FieldLabel(text = "Palavra-passe")
            Spacer(modifier = Modifier.height(6.dp))
            AppTextField(value = password, onValueChange = { password = it }, placeholder = "••••••••", isPassword = true)

            Spacer(modifier = Modifier.height(12.dp))
            FieldLabel(text = "Moeda")
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CurrencyPill(text = "€ EUR", selected = selectedCurrency == "EUR") { selectedCurrency = "EUR" }
                CurrencyPill(text = "$ USD", selected = selectedCurrency == "USD") { selectedCurrency = "USD" }
                CurrencyPill(text = "£ GBP", selected = selectedCurrency == "GBP") { selectedCurrency = "GBP" }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (termsAccepted) TealDark else Color.White)
                        .border(BorderStroke(1.dp, if (termsAccepted) TealDark else NeutralMid), RoundedCornerShape(4.dp))
                        .clickable { termsAccepted = !termsAccepted },
                    contentAlignment = Alignment.Center,
                ) {
                    if (termsAccepted) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = buildAnnotatedString {
                        append("Aceito os ")
                        withStyle(SpanStyle(color = TealDark)) { append("Termos de Serviço") }
                        append(" e a ")
                        withStyle(SpanStyle(color = TealDark)) { append("Política de Privacidade") }
                    },
                    fontSize = 11.sp,
                    color = TextHint,
                    lineHeight = 16.sp,
                )
            }

            if (state is AuthState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = (state as AuthState.Error).message, fontSize = 12.sp, color = CoralMid)
            }

            Spacer(modifier = Modifier.height(14.dp))
            PrimaryButton(
                text = if (state is AuthState.Loading) "A criar conta..." else "Criar conta",
                onClick = {
                    vm.register(
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        password = password,
                        currency = selectedCurrency,
                        profilePhotoUri = profilePhotoUri,
                    )
                },
                enabled = termsAccepted && state !is AuthState.Loading,
            )
        }
    }
}

@Composable
private fun CurrencyPill(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) TealLight else Color.White)
            .border(0.5.dp, if (selected) Color(0xFF5DCAA5) else NeutralMid, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(text = text, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal, color = if (selected) TealText else TextHint)
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterScreenPreview() {
    MyWalletTheme { RegisterScreen() }
}
