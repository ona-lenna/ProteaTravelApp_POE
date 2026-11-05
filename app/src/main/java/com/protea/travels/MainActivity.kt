package com.protea.travels

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.protea.travels.data.Repo
import com.protea.travels.data.local.Connectivity
import com.protea.travels.i18n.LocalStrings
import com.protea.travels.i18n.stringsFor
import com.protea.travels.push.NotificationHelper
import com.protea.travels.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Repo talks to Room (offline cache) and ConnectivityManager, so it needs an
        // application Context once, before any screen calls it.
        Repo.init(applicationContext)
        NotificationHelper.ensureChannel(this)

        setContent {
            val vm: AppVM = viewModel()

            // Android 13+ requires this permission before any notification -- including
            // an incoming FCM push -- can actually be shown.
            val notifPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
            LaunchedEffect(Unit) {
                if (android.os.Build.VERSION.SDK_INT >= 33) {
                    notifPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            // Per-app language: keeps the few system-drawn surfaces (e.g. the
            // notification channel) in step with the in-app language picker.
            LaunchedEffect(vm.language) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(vm.language.code))
            }

            // Offline mode with sync: watch connectivity for as long as the app is open,
            // and let AppVM know the moment it changes so it can sync queued work.
            LaunchedEffect(Unit) {
                Connectivity.observe(applicationContext).collect { online -> vm.onConnectivityChanged(online) }
            }

            CompositionLocalProvider(LocalStrings provides stringsFor(vm.language)) {
                ProteaTheme(vm.settings.darkMode) { ProteaApp(vm) }
            }
        }
    }

    // Facebook Login (single sign-on) delivers its result here; forward it to the
    // shared CallbackManager so SocialAuth.kt's FacebookSignInButton can react to it.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        FacebookAuthHub.callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}

@Composable
fun ProteaApp(vm: AppVM) {
    val nav = rememberNavController()
    val ctx = LocalContext.current
    val strings = LocalStrings.current
    LaunchedEffect(vm.message) {
        vm.message?.let { Toast.makeText(ctx, it, Toast.LENGTH_LONG).show(); vm.message = null }
    }
    val route = nav.currentBackStackEntryAsState().value?.destination?.route
    val tabs = listOf(Triple("home", strings.home, Icons.Default.Home),
        Triple("bookings", strings.trips, Icons.Default.ConfirmationNumber),
        Triple("settings", strings.settings, Icons.Default.Settings))
    fun clearTo(dest: String) = nav.navigate(dest) { popUpTo(nav.graph.id) { inclusive = true } }

    Scaffold(
        topBar = { if (vm.isOffline) OfflineBar(vm) },
        bottomBar = {
            if (route in tabs.map { it.first }) NavigationBar {
                tabs.forEach { (r, label, icon) ->
                    NavigationBarItem(selected = route == r, onClick = {
                        nav.navigate(r) { popUpTo("home"); launchSingleTop = true }
                    }, icon = { Icon(icon, label) }, label = { Text(label) })
                }
            }
        }
    ) { pad ->
        NavHost(nav, "splash", Modifier.padding(pad)) {
            composable("splash") { SplashScreen(Repo.isLoggedIn) { li -> clearTo(if (li) "home" else "welcome") } }
            composable("welcome") { WelcomeScreen({ nav.navigate("register") }, { nav.navigate("login") }) }
            composable("login") { LoginScreen(vm, { nav.popBackStack() }, { nav.navigate("register") }) { clearTo("home") } }
            composable("register") { RegisterScreen(vm, { nav.popBackStack() }) { clearTo("home") } }
            composable("home") { HomeScreen(vm) { nav.navigate("search") } }
            composable("search") { SearchScreen(vm, { nav.popBackStack() }) { nav.navigate("results") } }
            composable("results") { ResultsScreen(vm, { nav.popBackStack() }) { nav.navigate("bookings") { popUpTo("home") } } }
            composable("bookings") { BookingsScreen(vm) }
            composable("settings") { SettingsScreen(vm) { clearTo("welcome") } }
        }
    }
}

/** Thin banner shown app-wide whenever the device has no connection (offline mode). */
@Composable
private fun OfflineBar(vm: AppVM) {
    val strings = LocalStrings.current
    Surface(color = MaterialTheme.colorScheme.errorContainer) {
        Text(
            if (vm.pendingSyncCount > 0) "${strings.offlineCached} (${vm.pendingSyncCount} pending)" else strings.offlineCached,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}
