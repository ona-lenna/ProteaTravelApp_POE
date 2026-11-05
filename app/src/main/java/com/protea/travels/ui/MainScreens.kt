package com.protea.travels.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.protea.travels.AppVM
import com.protea.travels.data.*
import com.protea.travels.i18n.Language
import com.protea.travels.i18n.LocalStrings
import java.time.LocalTime

// ------------------------------------------------------------ HOME
@Composable
fun HomeScreen(vm: AppVM, onSearch: () -> Unit) {
    val strings = LocalStrings.current
    val hour = LocalTime.now().hour
    val greet = if (hour < 12) strings.goodMorning else if (hour < 18) strings.goodAfternoon else strings.goodEvening
    val s = vm.search
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())
        .statusBarsPadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Logo(false, 40)
        Text("$greet, ${vm.userName}", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Text(strings.whereTo, color = Color.Gray)

        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(strings.curatedSearch, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Picker(strings.from, s.from, Airports.options, Modifier.weight(1f)) { vm.updateSearch(s.copy(from = it)) }
                    Picker(strings.to, s.to, Airports.options, Modifier.weight(1f)) { vm.updateSearch(s.copy(to = it)) }
                }
                GoldButton(strings.searchFlights, onClick = onSearch)
            }
        }

        // Weather feature: shows as soon as a destination is picked anywhere in the app.
        WeatherCard(vm)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(strings.exclusiveDeals, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        }
        val deals = listOf(Triple("CPT", 2450, listOf(Color(0xFF5B3A6B), Color(0xFF1E2A4A))),
            Triple("ZNZ", 4890, listOf(Color(0xFF1B8DA6), Color(0xFF0E3B5C))),
            Triple("DUR", 1890, listOf(Color(0xFFCC7A2E), Color(0xFF6B2F1A))),
            Triple("DXB", 7990, listOf(Color(0xFFB08A2E), Color(0xFF3A2A0E))))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            items(deals) { (code, price, colors) ->
                Box(Modifier.width(230.dp).height(170.dp).background(Brush.verticalGradient(colors), RoundedCornerShape(24.dp))
                    .clickable { vm.updateSearch(s.copy(to = code)); onSearch() }.padding(16.dp)) {
                    Surface(color = Green, shape = RoundedCornerShape(10.dp)) {
                        Text("PROMO", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                    }
                    Column(Modifier.align(Alignment.BottomStart)) {
                        Text(Airports.all[code] ?: code, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Flights from ${money(price, vm.settings.currency)}", color = Gold, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(Color(0xFFE8F0EA)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Green)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(strings.activePriceAlerts, color = Green, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                if (vm.alerts.isEmpty()) Text(strings.noAlerts, color = Color(0xFF333333))
                vm.alerts.forEach { a ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Track ${a.from} to ${a.to} price drops", color = Color(0xFF1B1B1B), modifier = Modifier.weight(1f))
                        IconButton({ vm.removeAlert(a.id) }) { Icon(Icons.Default.Delete, "Remove", tint = Color(0xFF555555)) }
                    }
                }
                TextButton({ vm.addAlert() }) { Icon(Icons.Default.Notifications, null, tint = Green); Spacer(Modifier.width(6.dp)); Text(strings.addAlert, color = Green) }
            }
        }
    }
}

/** Weather feature: current conditions for the selected destination, via Open-Meteo (no API key). */
@Composable
fun WeatherCard(vm: AppVM) {
    val strings = LocalStrings.current
    val dest = vm.search.to
    if (dest.isEmpty()) return
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(Navy)) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.WbSunny, null, tint = Gold, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("${strings.weatherAt} ${Airports.all[dest] ?: dest}", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                val w = vm.weather
                when {
                    vm.weatherLoading -> Text("…", color = Color(0xFFC7CEE0), fontSize = 12.sp)
                    w != null -> Text("${w.tempC}°C, ${w.condition} · wind ${w.windKmh} km/h", color = Color(0xFFC7CEE0), fontSize = 12.sp)
                    else -> Text("Weather unavailable right now", color = Color(0xFFC7CEE0), fontSize = 12.sp)
                }
            }
        }
    }
}

// ------------------------------------------------------------ SEARCH
@Composable
fun SearchScreen(vm: AppVM, onBack: () -> Unit, onResults: () -> Unit) {
    val strings = LocalStrings.current
    val s = vm.search
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())
        .statusBarsPadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            Text(strings.searchFlights, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        }
        Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50)).padding(4.dp)) {
            listOf("Return" to strings.returnTrip, "One-way" to strings.oneWay).forEach { (t, label) ->
                val sel = s.tripType == t
                Box(Modifier.weight(1f).height(44.dp).background(if (sel) Navy else Color.Transparent, RoundedCornerShape(50))
                    .clickable { vm.updateSearch(s.copy(tripType = t)) }, contentAlignment = Alignment.Center) {
                    Text(label, color = if (sel) Gold else Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        }
        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Picker(strings.from, s.from, Airports.options) { vm.updateSearch(s.copy(from = it)) }
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    FilledIconButton({ vm.updateSearch(s.copy(from = s.to, to = s.from)) },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Gold, contentColor = Navy)) {
                        Icon(Icons.Default.SwapVert, "Swap")
                    }
                }
                Picker(strings.to, s.to, Airports.options) { vm.updateSearch(s.copy(to = it)) }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DateField(strings.departure, s.depart, Modifier.weight(1f)) { vm.updateSearch(s.copy(depart = it)) }
                    DateField(strings.returnDate, if (s.tripType == "Return") s.ret else "—", Modifier.weight(1f), s.tripType == "Return") { vm.updateSearch(s.copy(ret = it)) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        IconButton({ if (s.pax > 1) vm.updateSearch(s.copy(pax = s.pax - 1)) }) { Icon(Icons.Default.Remove, "Less") }
                        Text("${s.pax} ${strings.passengers}", fontWeight = FontWeight.SemiBold)
                        IconButton({ if (s.pax < 9) vm.updateSearch(s.copy(pax = s.pax + 1)) }) { Icon(Icons.Default.Add, "More") }
                    }
                    Picker(strings.cabinClass, s.cabin, cabinMultiplier.keys.map { it to it }, Modifier.weight(1f)) { vm.updateSearch(s.copy(cabin = it)) }
                }
            }
        }

        WeatherCard(vm)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("✔ ${strings.guaranteedPricing}", color = Green, fontSize = 13.sp)
            Text("✔ ${strings.vipAssist}", color = Green, fontSize = 13.sp)
        }
        GoldButton(if (vm.busy) "…" else strings.searchFlights, enabled = !vm.busy) { vm.runSearch(onResults) }
    }
}

// ------------------------------------------------------------ RESULTS
@Composable
fun ResultsScreen(vm: AppVM, onBack: () -> Unit, onBooked: () -> Unit) {
    val strings = LocalStrings.current
    val s = vm.search
    var pick by remember { mutableStateOf<Flight?>(null) }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            Column {
                Text("${s.from} → ${s.to}", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                Text("${s.depart}${if (s.tripType == "Return") "  •  back ${s.ret}" else ""}  •  ${s.pax} ${strings.passengers.lowercase()}  •  ${s.cabin}", color = Color.Gray, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        if (vm.isOffline) {
            Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(12.dp)) {
                Text(strings.offlineCached, modifier = Modifier.padding(10.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onErrorContainer)
            }
            Spacer(Modifier.height(10.dp))
        }
        if (vm.results.isEmpty()) Text("No flights found for this route. Try another.", color = Color.Gray)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(vm.results) { f ->
                Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(f.airline, fontWeight = FontWeight.Bold, color = Green)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${f.departTime}  →  ${f.arriveTime}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                            Text("${f.durationMin / 60}h ${f.durationMin % 60}m", color = Color.Gray)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(money(vm.total(f), vm.settings.currency), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                Text("${strings.total}, ${s.pax} ${strings.passengers.lowercase()}", color = Color.Gray, fontSize = 11.sp)
                            }
                            Button({ pick = f }, colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy)) { Text(strings.book) }
                        }
                    }
                }
            }
        }
    }
    pick?.let { f ->
        AlertDialog(onDismissRequest = { pick = null },
            title = { Text(strings.confirmBookingTitle) },
            text = { Text("${f.airline} ${f.from} → ${f.to}\nDeparts ${s.depart} at ${f.departTime}\n${s.pax} ${strings.passengers.lowercase()}, ${s.cabin}\n${strings.total} ${money(vm.total(f), vm.settings.currency)}") },
            confirmButton = { TextButton({ pick = null; vm.book(f, onBooked) }) { Text(strings.confirm) } },
            dismissButton = { TextButton({ pick = null }) { Text(strings.cancel) } })
    }
}

// ------------------------------------------------------------ BOOKINGS
@Composable
fun BookingsScreen(vm: AppVM) {
    val strings = LocalStrings.current
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().padding(20.dp)) {
        Text(strings.myTrips, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(12.dp))
        if (vm.bookings.isEmpty()) Text(strings.noTrips, color = Color.Gray)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(vm.bookings) { b ->
                Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${b.from} → ${b.to}", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                            Text("${b.airline} • ${b.departDate} ${b.departTime}", color = Color.Gray, fontSize = 13.sp)
                            if (b.returnDate.isNotEmpty()) Text("${strings.returnDate} ${b.returnDate}", color = Color.Gray, fontSize = 13.sp)
                            Text("${b.passengers} ${strings.passengers.lowercase()} • ${b.cabin} • ${b.tripType}", fontSize = 13.sp)
                            Text(money(b.total, vm.settings.currency), color = Green, fontWeight = FontWeight.Bold)
                        }
                        IconButton({ vm.cancelBooking(b.id) }) { Icon(Icons.Default.Delete, "Cancel booking") }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------ SETTINGS
@Composable
fun SettingsScreen(vm: AppVM, onLogout: () -> Unit) {
    val strings = LocalStrings.current
    val s = vm.settings
    var name by remember(vm.userName) { mutableStateOf(vm.userName) }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())
        .statusBarsPadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(strings.settings, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Text(Repo.email, color = Color.Gray)

        if (vm.pendingSyncCount > 0) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.errorContainer)) {
                Text("${vm.pendingSyncCount} action(s) waiting to sync once you're back online.",
                    modifier = Modifier.padding(12.dp), fontSize = 13.sp, color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }

        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(strings.profile, fontWeight = FontWeight.Bold)
                Field(strings.firstName, name, { name = it })
                OutlinedButton({ vm.saveName(name) }, Modifier.fillMaxWidth()) { Text(strings.saveName) }
            }
        }
        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(strings.preferences, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.darkMode, Modifier.weight(1f))
                    Switch(s.darkMode, { vm.updateSettings(s.copy(darkMode = it)) })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.notifications, Modifier.weight(1f))
                    Switch(s.notifications, { vm.updateSettings(s.copy(notifications = it)) })
                }
                Picker(strings.currency, s.currency, listOf("ZAR" to "South African Rand (R)", "USD" to "US Dollar (approx.)")) { vm.updateSettings(s.copy(currency = it)) }
                Picker(strings.defaultAirport, s.defaultOrigin, Airports.options) {
                    vm.updateSettings(s.copy(defaultOrigin = it)); vm.updateSearch(vm.search.copy(from = it))
                }
                // Multi-language support: switches every screen immediately, and is saved
                // to the user's Firestore profile so it follows them to any device.
                Picker(strings.language, s.language, Language.entries.map { it.code to it.displayName }) {
                    vm.updateSettings(s.copy(language = it))
                }
            }
        }
        OutlinedButton({ vm.forgotPassword(Repo.email) }, Modifier.fillMaxWidth().height(52.dp)) { Text(strings.sendReset) }
        GoldButton(strings.signOut) { vm.logout(); onLogout() }
    }
}
