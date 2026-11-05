package com.protea.travels.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.protea.travels.AppVM
import com.protea.travels.i18n.LocalStrings
import kotlinx.coroutines.delay

@Composable
fun Logo(dark: Boolean = true, size: Int = 56) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(size.dp).border(1.5.dp, Gold, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Flight, null, tint = Gold, modifier = Modifier.size((size * 0.5).dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Protea", color = Gold, fontWeight = FontWeight.ExtraBold, fontSize = (size * 0.5).sp)
            Text("TRAVELS", color = if (dark) Color.White else Gold, letterSpacing = 3.sp, fontSize = (size * 0.27).sp)
        }
    }
}

@Composable
fun SplashScreen(loggedIn: Boolean, onNext: (Boolean) -> Unit) {
    LaunchedEffect(Unit) { delay(1600); onNext(loggedIn) }
    Box(Modifier.fillMaxSize().background(Navy), contentAlignment = Alignment.Center) { Logo(true, 70) }
}

@Composable
fun WelcomeScreen(onStart: () -> Unit, onSignIn: () -> Unit) {
    val strings = LocalStrings.current
    Column(Modifier.fillMaxSize().background(Navy)) {
        // Drop your Table Mountain image in res/drawable and swap this Box for an Image() if you like
        Box(Modifier.fillMaxWidth().weight(1.1f).background(
            Brush.verticalGradient(listOf(Color(0xFFF2B233), Color(0xFFB9791A), Color(0xFF5A3A12)))),
            contentAlignment = Alignment.BottomStart) {
            Box(Modifier.padding(20.dp).background(Color(0x66000000), RoundedCornerShape(16.dp)).padding(12.dp)) { Logo(true, 44) }
        }
        Column(Modifier.weight(1f).padding(24.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(strings.welcomeHeadline, color = Gold, fontSize = 34.sp, fontWeight = FontWeight.Bold, lineHeight = 40.sp)
                Spacer(Modifier.height(14.dp))
                Text(strings.welcomeBody, color = Color.White, fontSize = 17.sp, lineHeight = 24.sp)
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GoldButton(strings.getStarted, onClick = onStart)
                OutlinedButton(onSignIn, Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Gold),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold)) { Text(strings.signIn, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun AuthShell(onBack: () -> Unit, title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())
        .statusBarsPadding().padding(24.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            Logo(false, 36)
        }
        Spacer(Modifier.height(48.dp))
        Text(title, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
        Text(subtitle, color = Color.Gray, fontSize = 16.sp, modifier = Modifier.padding(top = 6.dp, bottom = 22.dp))
        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp), content = content)
        }
    }
}

@Composable
fun LoginScreen(vm: AppVM, onBack: () -> Unit, onCreate: () -> Unit, onSuccess: () -> Unit) {
    val strings = LocalStrings.current
    var email by remember { mutableStateOf("") }
    var pw by remember { mutableStateOf("") }
    AuthShell(onBack, strings.welcomeBack, strings.signInSubtitle) {
        Field(strings.emailAddress, email, { email = it }, keyboard = KeyboardType.Email)
        Field(strings.password, pw, { pw = it }, password = true)
        Text(strings.forgotPassword, color = Green, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.End).clickableNoRipple { vm.forgotPassword(email) })
        GoldButton(if (vm.busy) "…" else strings.signIn, enabled = !vm.busy) { vm.login(email, pw, onSuccess) }
        // Single sign-on: Google and Facebook, both exchanged for a Firebase credential in Repo.kt
        SocialSignInRow(vm, onSuccess)
    }
}

@Composable
fun RegisterScreen(vm: AppVM, onBack: () -> Unit, onSuccess: () -> Unit) {
    val strings = LocalStrings.current
    var f by remember { mutableStateOf("") }; var l by remember { mutableStateOf("") }
    var e by remember { mutableStateOf("") }; var p by remember { mutableStateOf("") }; var c by remember { mutableStateOf("") }
    AuthShell(onBack, strings.joinProtea, strings.joinSubtitle) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Field(strings.firstName, f, { f = it }, Modifier.weight(1f)); Field(strings.lastName, l, { l = it }, Modifier.weight(1f))
        }
        Field(strings.emailAddress, e, { e = it }, keyboard = KeyboardType.Email)
        Field(strings.password, p, { p = it }, password = true)
        Field(strings.confirmPassword, c, { c = it }, password = true)
        GoldButton(if (vm.busy) "…" else strings.createAccount, enabled = !vm.busy) { vm.register(f, l, e, p, c, onSuccess) }
        SocialSignInRow(vm, onSuccess)
    }
}
