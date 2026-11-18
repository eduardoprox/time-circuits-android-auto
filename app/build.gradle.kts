// AGP 9 compila Kotlin sozinho; nao aplicar org.jetbrains.kotlin.android.
plugins {
    id("com.android.application")
}

android {
    namespace = "com.eduardoprox.timecircuits"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.eduardoprox.timecircuits"
        // Apps "parados" no Android Auto exigem Android 15 (API 35).
        minSdk = 35
        targetSdk = 37
        versionCode = 2
        versionName = "0.2-navegacao"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("androidx.car.app:app:1.7.0")
}
