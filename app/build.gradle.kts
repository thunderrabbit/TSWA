plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.robnugen.tswa"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.robnugen.tswa"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
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
                storePassword = System.getenv("TSWA_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("TSWA_KEY_ALIAS")
                keyPassword = System.getenv("TSWA_KEY_PASSWORD")
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
