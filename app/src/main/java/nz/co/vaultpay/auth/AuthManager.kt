package nz.co.vaultpay.auth

import android.util.Base64
import org.json.JSONObject

/**
 * [M3 / MASVS-AUTH-1, AUTH-2] Insecure authentication / authorization.
 * [M1 / MASVS-STORAGE-1] Hardcoded admin credentials.
 *
 * Two planted flaws:
 *  1) verifyJwt() accepts alg:"none" tokens -> forge any identity, no signature needed.
 *  2) isAdmin() trusts a client-side claim -> privilege escalation with a crafted token.
 *
 * SECURE: verify signature server-side with a fixed allow-list of algorithms;
 * never make authorization decisions from unverified client-held claims.
 */
object AuthManager {

    // WRONG: backdoor credentials in the binary.
    private const val ADMIN_USER = "admin"
    private const val ADMIN_PASS = "VaultP@y2024!"

    fun localLogin(user: String, pass: String): Boolean =
        user == ADMIN_USER && pass == ADMIN_PASS

    /**
     * WRONG: decodes the JWT payload WITHOUT verifying the signature, and honours alg:none.
     * A token with header {"alg":"none"} and payload {"sub":"1","role":"admin"} is accepted.
     */
    fun verifyJwt(jwt: String): JSONObject? {
        val parts = jwt.split(".")
        if (parts.size < 2) return null
        val header = JSONObject(String(Base64.decode(parts[0], Base64.URL_SAFE)))
        if (header.optString("alg").equals("none", true)) {
            // "unsecured JWT" path — trusted blindly
            return JSONObject(String(Base64.decode(parts[1], Base64.URL_SAFE)))
        }
        // (No real signature check on the else branch either — also broken on purpose.)
        return JSONObject(String(Base64.decode(parts[1], Base64.URL_SAFE)))
    }

    /** WRONG: authorization decided from an unverified client-side claim. */
    fun isAdmin(claims: JSONObject): Boolean = claims.optString("role") == "admin"
}
