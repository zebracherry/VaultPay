package nz.co.vaultpay.ui

import android.util.Log

/**
 * [M6 / MASVS-PRIVACY-3] PII and secrets written to Logcat; [M1] token logged too.
 *
 * Anything logged here is readable with `adb logcat` on a debuggable build and can be
 * captured by other apps on older Android or via bug-report dumps.
 *
 * SECURE: never log PII/secrets; strip logs in release; use a no-op logger for prod.
 */
object PrivacyLogger {
    private const val TAG = "VaultPay"

    fun logLogin(user: String, pan: String, token: String) {
        Log.d(TAG, "login user=$user pan=$pan token=$token") // full PAN + bearer token in the clear
    }

    fun logDeviceId(androidId: String) {
        // WRONG: sending a hardware-tied identifier to analytics with no consent (M6 / PRIVACY-1,4).
        Log.i(TAG, "analytics device_id=$androidId")
    }
}
