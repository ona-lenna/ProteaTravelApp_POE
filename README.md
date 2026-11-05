# Protea Travels (Android · Kotlin · Jetpack Compose · Firebase)

## Mockups

The screens below were the design reference the app was built against. Full-size originals are in `/mockups`.

![Protea Travels screen mockups: splash, welcome, sign in, create account, home and search flights](mockups/contact-sheet.png)

| Screen | File |
|---|---|
| Splash | `mockups/01-splash.jpeg` |
| Welcome | `mockups/02-welcome.jpeg` |
| Sign in | `mockups/03-signin.jpeg` |
| Create account | `mockups/04-register.jpeg` |
| Home | `mockups/05-home.jpeg` |
| Search flights | `mockups/06-search.jpeg` |

## 1. Install first
- Android Studio (Koala or newer) with the bundled JDK 17
- SDK Platform 34 (SDK Manager > SDK Platforms > Android 14 "UpsideDownCake")
- An emulator (Pixel, API 34) or a physical phone with USB debugging
- A free Google account for Firebase

## 2. Create the online backend (Firebase)
1. https://console.firebase.google.com > Add project > "ProteaTravels"
2. Project overview > Add app > Android. Package name MUST be: com.protea.travels. Register app.
3. Download google-services.json and put it in: ProteaTravels/app/google-services.json
4. Build > Authentication > Get started > Sign-in method > enable Email/Password
5. Build > Firestore Database > Create database > Start in production mode > pick a region (e.g. europe-west / africa if offered)
6. Firestore > Rules tab > paste the contents of firestore.rules > Publish

## 3. Open and run
1. Unzip. Android Studio > File > Open > select the ProteaTravels folder.
2. Let Gradle sync (needs internet). If asked about the Gradle wrapper, accept.
   If sync says the wrapper jar is missing: Settings > Build Tools > Gradle > "Use Gradle from: gradle-wrapper.properties" or choose the bundled Gradle.
3. Do NOT accept "upgrade AGP/Kotlin" prompts until it runs once (versions are pinned to work together).
4. Run > app. First login seeds the flights collection in Firestore.

## 4. Dependencies used (already in app/build.gradle.kts)
- Jetpack Compose BOM + Material3 + material-icons-extended
- navigation-compose, lifecycle-viewmodel-compose, activity-compose, appcompat (per-app language)
- Firebase BoM: firebase-auth-ktx, firebase-firestore-ktx, firebase-messaging-ktx
- kotlinx-coroutines-play-services (.await() on Firebase tasks)
- play-services-auth (Google Sign-In), facebook-login (Facebook Login)
- Room (room-runtime, room-ktx, room-compiler via KSP) for the offline cache
- OkHttp + org.json for the weather API call
- Plugins: com.google.gms.google-services, com.google.devtools.ksp

## 5. Rubric mapping
| Requirement | Where |
|---|---|
| Register / login | AuthScreens.kt, Repo.register/login. Passwords are hashed and stored by Firebase Auth; the app never stores them. |
| Single sign-on (Google & Facebook) | ui/SocialAuth.kt, Repo.signInWithGoogle/signInWithFacebook — see section 10 to configure |
| Settings | SettingsScreen (name, dark mode, currency, default airport, language, notifications) saved to Firestore per user |
| Online API + DB | Firebase Auth + Cloud Firestore (hosted by Google, accessed over HTTPS) |
| Flight search (return / one-way, dates, passengers, cabin) | SearchScreen + Repo.searchFlights |
| Results, booking, my trips (create/read/delete) | ResultsScreen, BookingsScreen |
| Deals and price alerts (create/read/delete) | HomeScreen |
| Offline mode with sync | data/local/* (Room), Repo.searchFlights/addBooking/addAlert/syncPending — see section 11 |
| Real-time push notifications | push/ProteaMessagingService.kt, push/NotificationHelper.kt — see section 12 |
| Multi-language support (English, isiZulu, Afrikaans) | i18n/Strings.kt, language picker in Settings — see section 13 |
| Weather feature | data/WeatherRepo.kt, WeatherCard in MainScreens.kt — see section 14 |

## 6. Demo script
Register > sign out > sign in > tap a deal > search return flight (note the weather card) > book > see it in Trips > cancel > add a price alert > change settings (dark mode, currency, language) > sign out and back in to show settings persisted in the cloud > turn on airplane mode, book a flight, show it queues, turn connectivity back on and show it syncs.

## 7. Before submitting
Firestore rules currently let signed-in users write to `flights` so the app can seed demo data. After the first run, change that line to `allow write: if false;` and republish.

## 8. Continuous integration (GitHub Actions)
`.github/workflows/android-ci.yml` builds the debug APK and runs lint on every push and pull request to `main`. It calls `./gradlew`, which this zip does **not** include (it's a binary generated per-project, not something to hand-copy between projects) — one extra step is needed before pushing:
1. Open the project in Android Studio once and let it sync (section 3). This generates `gradlew`, `gradlew.bat` and `gradle/wrapper/gradle-wrapper.jar` in the project root.
2. `git add -A` and commit those generated files along with the rest of the project — the CI workflow has nothing to run without them.
3. Push this project to a GitHub repository.
4. Go to **Settings > Secrets and variables > Actions > New repository secret**, name it `GOOGLE_SERVICES_JSON`, and paste the full contents of your `google-services.json` as the value (the file itself is never committed, since it's specific to your Firebase project).
5. Push a commit. The **Actions** tab shows the build running, and a downloadable `app-debug` APK once it succeeds.

See the project report for the fuller explanation of how GitHub and GitHub Actions were used.

## 9. Enable Room (offline mode) — nothing to configure
Room is local-only, so there is no console setup for it. The first time you search a route while online, that route's results are cached automatically; if you then lose connectivity and search the same route again, you'll see the cached results with an offline banner instead of an error. Booking a flight or adding a price alert while offline queues it locally (Settings shows how many actions are waiting); it's sent to Firestore automatically, with no action needed from the user, the moment the device reconnects.

## 10. Configure single sign-on (Google & Facebook)
**Google:**
1. Firebase console > Authentication > Sign-in method > enable **Google**.
2. Still on that screen, Firebase shows a **Web client ID** ending in `.apps.googleusercontent.com` — copy it.
3. Open `app/src/main/res/values/strings.xml` and replace `google_web_client_id` with that value.

**Facebook:**
1. Create an app at developers.facebook.com (type: Consumer), then Products > add **Facebook Login**.
2. In the Facebook app's Settings > Basic, copy the **App ID** and **Client Token** (Advanced section).
3. In `app/src/main/res/values/strings.xml`, replace `facebook_app_id` with the App ID, `facebook_client_token` with the Client Token, and `fb_login_protocol_scheme` with `fb` followed by your App ID (e.g. `fb1234567890123`).
4. Still in the Facebook app, add a package with package name `com.protea.travels`, your default activity class name `com.protea.travels.MainActivity`, and a key hash. Get the debug key hash by running (requires OpenSSL; on Windows use Git Bash):
   ```
   keytool -exportcert -alias androiddebugkey -keystore %USERPROFILE%\.android\debug.keystore | openssl sha1 -binary | openssl base64
   ```
   (the keystore password is `android`).
5. In Firebase console > Authentication > Sign-in method > enable **Facebook**, pasting in the same App ID and App Secret (Facebook app > Settings > Basic).

Until both are configured, their buttons will fail gracefully with an error toast rather than crashing the app — email/password sign-in works regardless.

## 11. Offline mode with sync — how it works
- `data/local/AppDatabase.kt` (Room) caches the last search per route and queues any booking or price alert made while offline.
- `data/local/Connectivity.kt` watches the device's network state; `MainActivity` collects it and calls `AppVM.onConnectivityChanged`.
- The moment the device goes from offline to online, `Repo.syncPending()` pushes every queued booking and alert to Firestore and clears the local queue.
- To test it: put the emulator/phone in airplane mode, search a previously-searched route (cached results appear), book a flight (it queues — Settings shows a pending count), then turn connectivity back on and watch it sync automatically.

## 12. Real-time notifications (Firebase Cloud Messaging)
The app is fully wired to **receive** a push the moment one is sent — `push/ProteaMessagingService.kt` shows it as a system notification even if the app is closed, and each device's FCM token is saved to `users/{uid}/fcmToken` on login so a push can be targeted at a specific user. FCM only defines how a push is *received*; actually **sending** one needs a small piece of server-side code. The example below is a Cloud Function that sends a push automatically whenever a new booking is added — deploy it with the Firebase CLI (`firebase deploy --only functions`) if your assessment expects a working end-to-end example:

```javascript
// functions/index.js
const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { getMessaging } = require("firebase-admin/messaging");
const admin = require("firebase-admin");
admin.initializeApp();

exports.onBookingCreated = onDocumentCreated(
  "users/{uid}/bookings/{bookingId}",
  async (event) => {
    const booking = event.data.data();
    const userDoc = await admin.firestore().doc(`users/${event.params.uid}`).get();
    const token = userDoc.get("fcmToken");
    if (!token) return;
    await getMessaging().send({
      token,
      notification: {
        title: "Booking confirmed",
        body: `${booking.from} → ${booking.to} on ${booking.departDate}`,
      },
    });
  }
);
```

Without deploying a Cloud Function, you can still demonstrate real-time delivery by sending a one-off test message from **Firebase console > Engage > Messaging > New campaign**, targeted at the app, while it's running on a device.

## 13. Multi-language support
The language picker in Settings switches the whole app immediately between **English**, **isiZulu** and **Afrikaans** (`i18n/Strings.kt`), and the choice is saved to the signed-in user's Firestore profile, so it follows them to any device rather than resetting per phone. The main navigation, authentication screens, Home, Search, Results and Settings are translated; validation and toast messages remain in English for this submission (noted as a possible next step). Translations were drafted for this project and, as with any shipped app, should be checked by a fluent speaker before real-world release.

## 14. Weather feature
`data/WeatherRepo.kt` calls **Open-Meteo** (open-meteo.com), a free weather API that needs no key or account, so there's nothing to configure here. A weather card showing the destination's current temperature, condition and wind appears on Home and Search the moment a destination airport is picked.

## 15. Automated tests
**Unit tests** (`app/src/test/...`, 54 tests) run on your computer in a few seconds — no emulator, no internet, no Firebase:

| File | What it checks |
|---|---|
| `ValidationTest` | email format, registration rules, login rules, every search-form rule (empty fields, same airport, past date, return before departure, bad date text) |
| `PricingTest` | trip total (passengers x cabin x return), `R2,450` / `US$132` formatting |
| `ModelsTest` | default settings and search values, airport labels |
| `WeatherTest` | weather-code wording, parsing of good/bad/empty API responses, coordinates for every airport |
| `StringsTest` | English, isiZulu and Afrikaans are all complete and genuinely different |

Run them in Android Studio: right-click `app/src/test/java` > **Run 'Tests in ...'**, or in a terminal `./gradlew testDebugUnitTest`. A green tick on every test means all pass; the HTML report is at `app/build/reports/tests/testDebugUnitTest/index.html`.

**Instrumented test** (`app/src/androidTest/.../RoomOfflineTest.kt`, 5 tests) checks the offline cache and sync queue using Room. It needs an emulator or phone: right-click the file > **Run 'RoomOfflineTest'**.

The same unit tests run automatically on every push through GitHub Actions (section 8).
