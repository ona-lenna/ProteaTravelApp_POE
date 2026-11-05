package com.protea.travels.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.protea.travels.data.Repo

/**
 * Real-time notifications (Firebase Cloud Messaging).
 *
 * This service receives a push the moment one is sent to this device's token --
 * including while the app is backgrounded or not running -- which is what makes it
 * "real-time" rather than something the app has to poll for. Two kinds of message
 * are expected:
 *  - a price-drop alert, sent by a Cloud Function that watches the `flights`
 *    collection and compares new prices against each user's `alerts` sub-collection;
 *  - a booking confirmation, sent by a Cloud Function triggered when a document is
 *    added to a user's `bookings` sub-collection.
 * A minimal example of that Cloud Function is documented in README section 12; FCM
 * only defines how the device *receives* a push, not what triggers one, so a small
 * piece of server-side code is required to actually send them.
 */
class ProteaMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: "Protea Travels"
        val body = message.notification?.body ?: message.data["body"] ?: "You have a new update."
        NotificationHelper.show(applicationContext, title, body)
    }

    override fun onNewToken(token: String) {
        // Save the token against this user's profile so a Cloud Function can target
        // their device specifically, rather than broadcasting to every installation.
        Repo.saveFcmTokenIfSignedIn(token)
    }
}
