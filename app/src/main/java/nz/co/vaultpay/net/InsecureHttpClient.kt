package nz.co.vaultpay.net

import okhttp3.OkHttpClient
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * [M5 / MASVS-NETWORK-1, NETWORK-2] Insecure communication.
 *
 * A TrustManager that accepts ANY certificate + a hostname verifier that never fails.
 * This makes MITM trivial and defeats even a system that would otherwise pin.
 *
 * Detect: MobSF flags "improper certificate validation". Confirm with Burp/mitmproxy:
 * traffic intercepts with no pinning bypass needed.
 *
 * SECURE: default OkHttpClient (validates the chain) + CertificatePinner for pinning.
 */
object InsecureHttpClient {

    // WRONG: base URL is plain HTTP.
    const val BASE_URL = "http://api.vaultpay.internal/"

    fun build(): OkHttpClient {
        val trustAll = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })
        val ctx = SSLContext.getInstance("SSL")
        ctx.init(null, trustAll, java.security.SecureRandom())

        return OkHttpClient.Builder()
            .sslSocketFactory(ctx.socketFactory, trustAll[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true } // accept any hostname
            .build()
    }
}
