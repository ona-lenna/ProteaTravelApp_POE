package com.protea.travels.i18n

import androidx.compose.runtime.compositionLocalOf

/**
 * Multi-language support (POE requirement: at least two South African languages).
 *
 * Rather than Android's XML string-resource system -- which only switches on the
 * device's system locale -- this app lets the signed-in user pick a language from
 * within Settings, independent of the phone's locale, and the choice is saved to
 * their Firestore profile so it follows them to any device. AppCompatDelegate's
 * per-app language API (see MainActivity) is used alongside this so the small
 * number of system-drawn surfaces (e.g. the notification channel name) also
 * respect the choice.
 *
 * Translations below cover the primary navigation, authentication and booking
 * flows. They were drafted for this assessment and, like any shipped translation,
 * should be reviewed by a fluent speaker before real-world release.
 */
enum class Language(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    ZULU("zu", "isiZulu"),
    AFRIKAANS("af", "Afrikaans")
}

data class AppStrings(
    // Welcome / onboarding
    val welcomeHeadline: String, val welcomeBody: String, val getStarted: String, val signIn: String,
    // Auth
    val welcomeBack: String, val signInSubtitle: String, val emailAddress: String, val password: String,
    val forgotPassword: String, val dontHaveAccount: String, val createOne: String,
    val joinProtea: String, val joinSubtitle: String, val firstName: String, val lastName: String,
    val confirmPassword: String, val createAccount: String, val alreadyHaveAccount: String,
    val orContinueWith: String, val continueWithGoogle: String, val continueWithFacebook: String,
    // Tabs
    val home: String, val trips: String, val settings: String,
    // Home
    val goodMorning: String, val goodAfternoon: String, val goodEvening: String, val whereTo: String,
    val curatedSearch: String, val from: String, val to: String, val searchFlights: String,
    val exclusiveDeals: String, val activePriceAlerts: String, val addAlert: String, val noAlerts: String,
    // Search
    val returnTrip: String, val oneWay: String, val departure: String, val returnDate: String,
    val passengers: String, val cabinClass: String, val guaranteedPricing: String, val vipAssist: String,
    val weatherAt: String,
    // Results / booking
    val book: String, val confirmBookingTitle: String, val confirm: String, val cancel: String, val total: String,
    // Trips
    val myTrips: String, val noTrips: String,
    // Settings
    val profile: String, val saveName: String, val preferences: String, val darkMode: String,
    val notifications: String, val currency: String, val defaultAirport: String, val language: String,
    val signOut: String, val sendReset: String,
    // Offline / sync
    val offlineCached: String, val offlineQueued: String, val syncedNow: String
)

val English = AppStrings(
    welcomeHeadline = "From South Africa\nto the World", welcomeBody = "Discover premium, curated flights worldwide. Sandton luxury meets modern travel technology at your fingertips.",
    getStarted = "Get Started", signIn = "Sign In",
    welcomeBack = "Welcome Back", signInSubtitle = "Sign in to access your luxury itineraries and deals.",
    emailAddress = "Email address", password = "Password", forgotPassword = "Forgot Password?",
    dontHaveAccount = "Don't have an account?", createOne = "Create one",
    joinProtea = "Join Protea Travels", joinSubtitle = "Experience premium aviation designed with South African pride.",
    firstName = "First name", lastName = "Last name", confirmPassword = "Confirm password",
    createAccount = "Create Account", alreadyHaveAccount = "Already have an account?",
    orContinueWith = "or continue with", continueWithGoogle = "Continue with Google", continueWithFacebook = "Continue with Facebook",
    home = "Home", trips = "Trips", settings = "Settings",
    goodMorning = "Good morning", goodAfternoon = "Good afternoon", goodEvening = "Good evening", whereTo = "Where to?",
    curatedSearch = "Curated Flight Search", from = "From", to = "To", searchFlights = "Search Flights",
    exclusiveDeals = "Protea Exclusive Deals", activePriceAlerts = "Active Price Alerts",
    addAlert = "Add alert for selected route", noAlerts = "No alerts yet. Choose a route above, then tap below.",
    returnTrip = "Return", oneWay = "One-way", departure = "Departure", returnDate = "Return",
    passengers = "Passengers", cabinClass = "Cabin class", guaranteedPricing = "Guaranteed Pricing",
    vipAssist = "24/7 Sandton VIP Assist", weatherAt = "Weather at",
    book = "Book", confirmBookingTitle = "Confirm booking", confirm = "Confirm", cancel = "Cancel", total = "Total",
    myTrips = "My Trips", noTrips = "No trips booked yet. Search for a flight to get started.",
    profile = "Profile", saveName = "Save name", preferences = "Preferences", darkMode = "Dark mode",
    notifications = "Price-drop notifications", currency = "Currency", defaultAirport = "Default departure airport",
    language = "Language", signOut = "Sign Out", sendReset = "Send password reset email",
    offlineCached = "You're offline — showing your last saved results.",
    offlineQueued = "You're offline. This booking will be sent automatically once you're back online.",
    syncedNow = "Back online — your offline bookings have been synced."
)

val Zulu = AppStrings(
    welcomeHeadline = "Kusukela eNingizimu Afrika\nukuya emhlabeni wonke", welcomeBody = "Thola amabhanoyi akhethekile nawezinga eliphezulu emhlabeni wonke. Ubunkone base-Sandton nobuchwepheshe bokuhamba bomangwane ezandleni zakho.",
    getStarted = "Qala Manje", signIn = "Ngena",
    welcomeBack = "Siyakwamukela futhi", signInSubtitle = "Ngena ukuze ufinyelele ezinhlelweni zakho zohambo nezivumelwano.",
    emailAddress = "Ikheli le-imeyili", password = "Iphasiwedi", forgotPassword = "Ukhohlwe iphasiwedi?",
    dontHaveAccount = "Awunayo i-akhawunti?", createOne = "Yakha eyakho",
    joinProtea = "Joyina i-Protea Travels", joinSubtitle = "Thola uhambo lwezinga eliphezulu oluklanyelwe ngokuziqhenya kweNingizimu Afrika.",
    firstName = "Igama lokuqala", lastName = "Isibongo", confirmPassword = "Qinisekisa iphasiwedi",
    createAccount = "Yakha i-Akhawunti", alreadyHaveAccount = "Usunayo i-akhawunti?",
    orContinueWith = "noma qhubeka nge", continueWithGoogle = "Qhubeka ngeGoogle", continueWithFacebook = "Qhubeka ngeFacebook",
    home = "Ikhaya", trips = "Uhambo", settings = "Izilungiselelo",
    goodMorning = "Sawubona ekuseni", goodAfternoon = "Sawubona emini", goodEvening = "Sawubona kusihlwa", whereTo = "Uya kuphi?",
    curatedSearch = "Ukusesha Kwamabhanoyi Akhethiwe", from = "Kusuka", to = "Kuya", searchFlights = "Sesha Amabhanoyi",
    exclusiveDeals = "Izivumelwano Ezikhethekile ze-Protea", activePriceAlerts = "Izexwayiso Zentengo Ezisebenzayo",
    addAlert = "Engeza isexwayiso sohambo olukhethiwe", noAlerts = "Azikho izexwayiso okwamanje. Khetha uhambo ngenhla.",
    returnTrip = "Buyela", oneWay = "Indlela eyodwa", departure = "Ukusuka", returnDate = "Ukubuya",
    passengers = "Abagibeli", cabinClass = "Isigaba sekebhini", guaranteedPricing = "Intengo Eqinisekisiwe",
    vipAssist = "Usizo lwe-VIP lwe-Sandton 24/7", weatherAt = "Isimo sezulu e",
    book = "Bhuka", confirmBookingTitle = "Qinisekisa ukubhuka", confirm = "Qinisekisa", cancel = "Khansela", total = "Isamba",
    myTrips = "Uhambo Lwami", noTrips = "Awukabhuki uhambo. Sesha ibhanoyi ukuze uqale.",
    profile = "Iphrofayili", saveName = "Londoloza igama", preferences = "Okukhethwayo", darkMode = "Imodi emnyama",
    notifications = "Izaziso zokwehla kwentengo", currency = "Uhlobo lwemali", defaultAirport = "Isikhumulo sezindiza esizenzakalelayo",
    language = "Ulimi", signOut = "Phuma", sendReset = "Thumela i-imeyili yokusetha kabusha iphasiwedi",
    offlineCached = "Awuxhunyiwe ku-inthanethi — kuboniswa imiphumela yakho yokugcina.",
    offlineQueued = "Awuxhunyiwe. Lokhu kubhukha kuzothunyelwa ngokuzenzakalela lapho usuxhunyiwe futhi.",
    syncedNow = "Usuxhunyiwe futhi — ukubhuka kwakho okwakungaxhunyiwe sekuthunyelwe."
)

val Afrikaans = AppStrings(
    welcomeHeadline = "Van Suid-Afrika\nna die Wêreld", welcomeBody = "Ontdek premium, saamgestelde vlugte wêreldwyd. Sandton-weelde ontmoet moderne reistegnologie tot jou beskikking.",
    getStarted = "Begin Nou", signIn = "Meld Aan",
    welcomeBack = "Welkom Terug", signInSubtitle = "Meld aan om toegang tot jou luukse reisplanne en aanbiedinge te kry.",
    emailAddress = "E-posadres", password = "Wagwoord", forgotPassword = "Wagwoord vergeet?",
    dontHaveAccount = "Het jy nie 'n rekening nie?", createOne = "Skep een",
    joinProtea = "Sluit aan by Protea Travels", joinSubtitle = "Ervaar premium lugvaart ontwerp met Suid-Afrikaanse trots.",
    firstName = "Voornaam", lastName = "Van", confirmPassword = "Bevestig wagwoord",
    createAccount = "Skep Rekening", alreadyHaveAccount = "Het jy reeds 'n rekening?",
    orContinueWith = "of gaan voort met", continueWithGoogle = "Gaan voort met Google", continueWithFacebook = "Gaan voort met Facebook",
    home = "Tuis", trips = "Reise", settings = "Instellings",
    goodMorning = "Goeie môre", goodAfternoon = "Goeie middag", goodEvening = "Goeie naand", whereTo = "Waarheen?",
    curatedSearch = "Saamgestelde Vlugsoektog", from = "Van", to = "Na", searchFlights = "Soek Vlugte",
    exclusiveDeals = "Protea Eksklusiewe Aanbiedinge", activePriceAlerts = "Aktiewe Prysbelang",
    addAlert = "Voeg waarskuwing vir gekose roete by", noAlerts = "Nog geen waarskuwings nie. Kies 'n roete hierbo.",
    returnTrip = "Retoer", oneWay = "Enkel", departure = "Vertrek", returnDate = "Terugkeer",
    passengers = "Passasiers", cabinClass = "Kajuitklas", guaranteedPricing = "Gewaarborgde Pryse",
    vipAssist = "24/7 Sandton VIP-bystand", weatherAt = "Weer in",
    book = "Bespreek", confirmBookingTitle = "Bevestig bespreking", confirm = "Bevestig", cancel = "Kanselleer", total = "Totaal",
    myTrips = "My Reise", noTrips = "Nog geen reise bespreek nie. Soek 'n vlug om te begin.",
    profile = "Profiel", saveName = "Stoor naam", preferences = "Voorkeure", darkMode = "Donker modus",
    notifications = "Prysdaling-kennisgewings", currency = "Geldeenheid", defaultAirport = "Verstek vertreklughawe",
    language = "Taal", signOut = "Teken Uit", sendReset = "Stuur wagwoord-herstel-e-pos",
    offlineCached = "Jy is vanlyn — jou laaste gestoorde resultate word gewys.",
    offlineQueued = "Jy is vanlyn. Hierdie bespreking word outomaties gestuur sodra jy weer aanlyn is.",
    syncedNow = "Weer aanlyn — jou vanlyn besprekings is gesinkroniseer."
)

fun stringsFor(language: Language): AppStrings = when (language) {
    Language.ENGLISH -> English
    Language.ZULU -> Zulu
    Language.AFRIKAANS -> Afrikaans
}

val LocalStrings = compositionLocalOf { English }
