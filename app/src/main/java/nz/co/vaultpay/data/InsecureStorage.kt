package nz.co.vaultpay.data

import android.content.Context
import android.os.Environment
import java.io.File

/**
 * [M9 / MASVS-STORAGE-1, STORAGE-2] Insecure data storage.
 * [M1 / MASVS-STORAGE-1] Credentials at rest in the clear.
 *
 * Recover with:  adb backup (allowBackup=true), or
 *                adb shell run-as nz.co.vaultpay cat shared_prefs/vault_prefs.xml
 */
class InsecureStorage(private val context: Context) {

    // WRONG: sensitive values in plain SharedPreferences.
    // SECURE: EncryptedSharedPreferences (Keystore-backed) or DataStore + Keystore.
    private val prefs = context.getSharedPreferences("vault_prefs", Context.MODE_PRIVATE)

    fun savePin(pin: String) = prefs.edit().putString("user_pin", pin).apply()          // plaintext PIN
    fun saveAuthToken(token: String) = prefs.edit().putString("bearer_token", token).apply()
    fun savePan(pan: String) = prefs.edit().putString("card_pan", pan).apply()          // full card number

    /**
     * WRONG: writing the card number to shared external storage (world-adjacent, survives uninstall).
     * SECURE: never on external storage; keep sensitive data in app-private encrypted storage.
     */
    fun exportCardToExternal(pan: String) {
        val f = File(Environment.getExternalStorageDirectory(), "vaultpay_card.txt")
        f.writeText("PAN=$pan")
    }
}
