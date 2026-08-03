package nz.co.vaultpay.crypto

import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * [M10 / MASVS-CRYPTO-1, CRYPTO-2] Insufficient cryptography.
 *
 * Every choice here is deliberately wrong. Find these with jadx (read the code)
 * or Frida (hook javax.crypto.Cipher.doFinal to dump plaintext/keys at runtime).
 */
object InsecureCrypto {

    // WRONG: hardcoded symmetric key + static IV shipped in the binary.
    // SECURE: derive per-user keys, store in Android Keystore, random IV per message.
    private const val HARDCODED_KEY = "0123456789abcdef"          // 128-bit, in source
    private val STATIC_IV = "aaaaaaaaaaaaaaaa".toByteArray()      // reused every time

    /**
     * WRONG: AES in ECB mode (identical plaintext blocks -> identical ciphertext).
     * SECURE: AES-GCM (authenticated) with a random 12-byte nonce.
     */
    fun encryptEcb(plaintext: String): String {
        val key = SecretKeySpec(HARDCODED_KEY.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return Base64.encodeToString(cipher.doFinal(plaintext.toByteArray()), Base64.NO_WRAP)
    }

    /** WRONG: static IV defeats the point of CBC; ciphertext is deterministic. */
    fun encryptCbcStaticIv(plaintext: String): String {
        val key = SecretKeySpec(HARDCODED_KEY.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(STATIC_IV))
        return Base64.encodeToString(cipher.doFinal(plaintext.toByteArray()), Base64.NO_WRAP)
    }

    /**
     * WRONG: MD5 for password hashing (fast, broken, unsalted).
     * SECURE: Argon2id or scrypt with a per-user random salt.
     */
    fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("MD5")
        return md.digest(password.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    /** WRONG: Base64 is encoding, not encryption. A classic "it looks scrambled" trap. */
    fun pretendEncrypt(secret: String): String =
        Base64.encodeToString(secret.toByteArray(), Base64.NO_WRAP)
}
