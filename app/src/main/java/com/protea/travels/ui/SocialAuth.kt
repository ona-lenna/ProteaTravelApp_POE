package com.protea.travels.ui

import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.protea.travels.AppVM
import com.protea.travels.FacebookAuthHub
import com.protea.travels.R

/**
 * Single sign-on (Google). Launches the standard Google account picker and, on
 * success, exchanges the returned ID token for a Firebase credential via
 * AppVM.signInWithGoogle -- see Repo.signInWithGoogle.
 */
@Composable
fun GoogleSignInButton(vm: AppVM, onSuccess: () -> Unit) {
    val context = LocalContext.current
    val webClientId = stringResource(R.string.google_web_client_id)
    val client = remember {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, options)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        runCatching {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data).getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) vm.signInWithGoogle(idToken, onSuccess) else vm.message = "Google sign-in did not return a token"
        }.onFailure { vm.message = "Google sign-in was cancelled or failed" }
    }
    OutlinedButton(
        onClick = { launcher.launch(client.signInIntent) },
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(50)
    ) {
        LetterBadge("G", Color(0xFF4285F4))
        Spacer(Modifier.width(10.dp))
        Text("Continue with Google")
    }
}

/**
 * Single sign-on (Facebook). Uses the Facebook Login SDK's own UI, then exchanges
 * the resulting access token for a Firebase credential via AppVM.signInWithFacebook
 * -- see Repo.signInWithFacebook. Requires MainActivity to forward onActivityResult
 * to FacebookAuthHub.callbackManager (already wired there).
 */
@Composable
fun FacebookSignInButton(vm: AppVM, onSuccess: () -> Unit) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val callback = object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                vm.signInWithFacebook(result.accessToken.token, onSuccess)
            }
            override fun onCancel() { /* user backed out; nothing to do */ }
            override fun onError(error: FacebookException) { vm.message = "Facebook sign-in failed" }
        }
        LoginManager.getInstance().registerCallback(FacebookAuthHub.callbackManager, callback)
        onDispose { LoginManager.getInstance().unregisterCallback(FacebookAuthHub.callbackManager) }
    }
    OutlinedButton(
        onClick = {
            LoginManager.getInstance().logInWithReadPermissions(
                context as ActivityResultRegistryOwner, FacebookAuthHub.callbackManager, listOf("email", "public_profile")
            )
        },
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1877F2))
    ) {
        LetterBadge("f", Color(0xFF1877F2))
        Spacer(Modifier.width(10.dp))
        Text("Continue with Facebook")
    }
}

/**
 * Material's icon set has no Google/Facebook brand marks, so a small lettered badge
 * stands in for the logo here. Swap for the official brand assets (res/drawable) if
 * your assessment requires the exact logos.
 */
@Composable
private fun LetterBadge(letter: String, color: Color) {
    Box(
        modifier = Modifier.size(20.dp).background(color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(letter, color = Color.White, fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    }
}

@Composable
fun SocialSignInRow(vm: AppVM, onSuccess: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Divider(Modifier.weight(1f))
            Text("or continue with", style = MaterialTheme.typography.labelMedium)
            Divider(Modifier.weight(1f))
        }
        GoogleSignInButton(vm, onSuccess)
        FacebookSignInButton(vm, onSuccess)
    }
}
