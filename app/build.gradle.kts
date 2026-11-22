plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// The base version (MAJOR.MINOR.PATCH; see docs/VERSIONING.md). CI passes
// tswaVersionName with its run number appended, so the versionName inside
// the binary, the version shown in the app and the Play Store listing are
// one and the same string.
val tswaBaseVersionName = "0.3.2"

android {
    namespace = "com.robnugen.tswa"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.robnugen.tswa"
        minSdk = 26
        targetSdk = 34
        versionCode = (project.findProperty("tswaVersionCode") as String?)?.toIntOrNull() ?: 1
        versionName = (project.findProperty("tswaVersionName") as String?) ?: tswaBaseVersionName
    }

    // Release signing is supplied by GitHub Secrets in CI (see docs/SIGNING.md).
    // When the env vars are absent (local dev, UI iteration), release builds
    // are simply left unsigned and the debug build is what we install.
    val keystorePath = System.getenv("TSWA_KEYSTORE_PATH")
    if (keystorePath != null) {
        signingConfigs {
            create("release") {
                storeFile = file(keystorePath)
                storeType = "PKCS12"
                storePassword = System.getenv("TSWA_KEYSTORE_PASSWORD")?.trim()
                keyAlias = System.getenv("TSWA_KEY_ALIAS")?.trim()
                keyPassword = System.getenv("TSWA_KEYSTORE_PASSWORD")?.trim() // PKCS12: key password == store password by construction
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (keystorePath != null) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}
