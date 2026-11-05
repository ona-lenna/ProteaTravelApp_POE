package com.protea.travels

import com.facebook.CallbackManager

/**
 * A single shared Facebook CallbackManager. The Facebook SDK delivers its login
 * result through Activity.onActivityResult, so MainActivity forwards to this one
 * instance, and any Composable that starts a Facebook login registers a callback
 * on it. Kept as a tiny top-level singleton rather than passed through AppVM,
 * since it is pure plumbing with no app state of its own.
 */
object FacebookAuthHub {
    val callbackManager: CallbackManager = CallbackManager.Factory.create()
}
