plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "nz.co.vaultpay"
    compileSdk = 34

    defaultConfig {
        applicationId = "nz.co.vaultpay"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // [M1 / MASVS-STORAGE-1] Secret baked into BuildConfig -> ends up in the APK.
        // Extract with:  jadx -> BuildConfig.java, or `strings classes.dex | grep -i key`
        buildConfigField("String", "PAYMENTS_API_KEY", "\"VAULTPAY_DEMO_FAKE_APIKEY_do_not_use_1234\"")
    }

    buildTypes {
        release {
            // [M7 / MASVS-RESILIENCE-3] No R8/obfuscation on release -> jadx output is fully readable.
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
        debug {
            // [M8 / MASVS-CODE-3] Debuggable + backup allowed on a build you might ship.
            isDebuggable = true
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    composeOptions { }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation(platform("androidx.compose:compose-bom:2024.09.02"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")

    // Room (used insecurely for M4 raw-query SQLi and M9 unencrypted DB)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // [M2 / MASVS-CODE-2] Deliberately pinned OLD OkHttp with published CVEs.
    // Current is 4.12.x. 3.12.x is years out of support. MobSF/OWASP-dependency-check will flag it.
    implementation("com.squareup.okhttp3:okhttp:3.12.1")
    implementation("com.squareup.retrofit2:retrofit:2.5.0")
}
