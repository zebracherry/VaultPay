# VaultPay 💳🔓

**A modern, intentionally vulnerable Android app for learning mobile penetration testing.**

VaultPay is a mock fintech wallet built on a current Android stack (Kotlin · Jetpack Compose ·
Retrofit/OkHttp · Room) and deliberately seeded with every risk in the **OWASP Mobile Top 10
(2024)**, each cross-mapped to an **OWASP MASVS v2.1.0** control. It's a practice range: find the
bugs statically and dynamically, then read the "secure counterpart" notes to learn the fix.

> ## ⚠️ Intentionally vulnerable — read before you build
> This app is insecure **by design**. Build and run it **only** on an emulator or a dedicated,
> disposable test device. **Never** install it on a phone you use, connect it to real accounts or
> networks, or publish a built APK anywhere. You are responsible for keeping it contained.

---

## Why VaultPay?

The apps most people learn on — DIVA, InsecureBankv2 — target the **2016** Mobile Top 10 and
pre-Jetpack Java, and their repos have been dormant for years. VaultPay is built against the
**current** standard, so the vulnerabilities, the code patterns, and the MASVS/MASTG IDs all match
what you'd actually write in a 2026 assessment report.

Every planted flaw ships with:
- a **deliberately wrong** implementation you can find with static and dynamic tooling, and
- a **`SECURE:` note** in the code describing the correct approach — so the app is also a
  remediation reference, not just a target.

## Who it's for

- People new to **mobile** pentesting who already understand web/app security.
- Anyone building a **MASVS/MASTG-aligned** testing methodology from scratch.
- Trainers and teams who want a modern, self-contained lab target for workshops or CTF-style drills.

---

## What's inside — OWASP Mobile Top 10 (2024) coverage

| Risk | Where it lives | MASVS v2.1.0 | How to test |
|---|---|---|---|
| **M1 Improper Credential Usage** | `BuildConfig.PAYMENTS_API_KEY`; `AuthManager` admin creds; token in `InsecureStorage` | STORAGE-1, CRYPTO-1 | jadx to `BuildConfig`, MobSF secret scan, `strings classes.dex` |
| **M2 Inadequate Supply Chain Security** | Pinned `okhttp:3.12.1` / `retrofit:2.5.0` | CODE-2 | MobSF dep scan, `./gradlew app:dependencies` |
| **M3 Insecure Authentication/Authorization** | `AuthManager.verifyJwt` (alg:none), `AuthManager.isAdmin` (client claim) | AUTH-1, AUTH-2 | Forge `{"alg":"none"}` JWT; Frida hook; Burp on login |
| **M4 Insufficient Input/Output Validation** | `VaultRawQueries.loginRawSql` (SQLi); `DeepLinkWebViewActivity` (WebView XSS + JS bridge) | CODE-4, PLATFORM-2 | `' OR '1'='1' --`; `adb am start` the deep link |
| **M5 Insecure Communication** | `InsecureHttpClient` (trust-all TLS, HTTP); `network_security_config.xml` | NETWORK-1, NETWORK-2 | Burp / mitmproxy — no pinning to bypass |
| **M6 Inadequate Privacy Controls** | `PrivacyLogger` (PII/PAN/token to Logcat, device ID to analytics); excess perms | PRIVACY-1..4 | `adb logcat \| grep VaultPay`; MobSF perms |
| **M7 Insufficient Binary Protections** | `minifyEnabled=false`, no obfuscation, no root/tamper checks | RESILIENCE-1..4 | jadx (fully readable); Frida attaches freely |
| **M8 Security Misconfiguration** | `debuggable=true`, `allowBackup=true`, exported activity + provider | PLATFORM-1, CODE-3 | MobSF manifest; `adb backup`; query exported provider |
| **M9 Insecure Data Storage** | `InsecureStorage` (plaintext prefs, external-storage export); no `FLAG_SECURE` | STORAGE-1, STORAGE-2 | `run-as … cat shared_prefs/…`; `adb pull` |
| **M10 Insufficient Cryptography** | `InsecureCrypto` (AES-ECB, hardcoded key + static IV, MD5, Base64-as-crypto) | CRYPTO-1, CRYPTO-2 | jadx crypto review; Frida hook `Cipher.doFinal` |

---

## Project layout

```
VaultPay/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle/wrapper/gradle-wrapper.properties
├── .gitignore · LICENSE · CONTRIBUTING.md
└── app/
    ├── build.gradle.kts                 # M1, M2, M7, M8
    └── src/main/
        ├── AndroidManifest.xml          # M4, M5, M6, M8
        ├── res/xml/network_security_config.xml   # M5
        └── java/nz/co/vaultpay/
            ├── auth/AuthManager.kt              # M3, M1
            ├── crypto/InsecureCrypto.kt         # M10
            ├── data/InsecureStorage.kt          # M9, M1
            ├── data/VaultDao.kt                 # M4 (+ safe reference)
            ├── net/InsecureHttpClient.kt        # M5
            ├── ui/PrivacyLogger.kt              # M6
            └── web/DeepLinkWebViewActivity.kt   # M4
```

> **Scaffold, not a prebuilt APK.** The security-relevant code is complete. When you open the
> project in Android Studio it generates the Gradle wrapper JAR and prompts for the remaining glue:
> a `MainActivity`, the Room `@Database` + `UserEntity`, and a small Compose login screen that calls
> `VaultRawQueries.loginRawSql(...)` and `AuthManager.verifyJwt(...)`. Wiring that glue is itself
> good Android practice.

---

## Quick start

**Requirements:** Android Studio (Koala or newer), an emulator (AVD) or disposable test device,
and — for the testing side — MobSF, jadx, Frida, and Burp Suite or mitmproxy.

```bash
git clone https://github.com/zebracherry/VaultPay.git
cd VaultPay
# open in Android Studio, let Gradle sync, add the glue it prompts for, then:
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Suggested learning path

1. **Static first.** Run the APK through **MobSF**; confirm each hit in **jadx** source. You should
   see secrets (M1), the outdated OkHttp (M2), trust-all TLS (M5), exported components + backup
   (M8), and excessive permissions (M6).
2. **Storage.** `adb shell run-as nz.co.vaultpay cat shared_prefs/vault_prefs.xml` (M9, M1);
   `adb pull /sdcard/vaultpay_card.txt` (external write).
3. **Network.** Proxy the emulator through Burp/mitmproxy — traffic intercepts with no pinning to
   defeat (M5).
4. **Dynamic.** Attach **Frida**: hook `javax.crypto.Cipher.doFinal` to dump plaintext/keys (M10);
   hook `AuthManager.verifyJwt` to watch the alg:none bypass (M3).
5. **Components.** `adb shell am start -a android.intent.action.VIEW -d "vaultpay://open?url=…"
   nz.co.vaultpay` (M4); query the exported `VaultProvider` (M8).
6. **Report.** For each finding, cite the **MASVS** control ID from the table and pull the matching
   **MASTG** test case (`MASTG-TEST-…`) as your evidence method — the same "requirement ID +
   reproducible test" structure as a WSTG-based web report.

---

## Standards reference

- **OWASP Mobile Top 10 (2024)** — the risk list VaultPay is built against.
- **OWASP MASVS v2.1.0** — 8 categories, 24 controls; the *what to verify*. (The old L1/L2/R levels
  are retired; risk tiers now live in MASTG as the MAS-L1 / MAS-L2 / MAS-R profiles.)
- **OWASP MASTG** — the *how to verify*: per-requirement test cases mapped to MASVS.

## Contributing

New intentionally-vulnerable scenarios and doc/test improvements are welcome — see
[CONTRIBUTING.md](CONTRIBUTING.md). Every flaw must be intentional, documented with its M-risk and
MASVS IDs, and emulator-safe.

## License

[MIT](LICENSE), with an additional intentionally-vulnerable-software notice. Use for education and
authorised testing only.
