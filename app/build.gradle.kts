plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.casinolaskrasnodar"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.casinolaskrasnodar"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {


    implementation (libs.postgrest.kt) // Для REST API
    implementation (libs.ktor.client.okhttp) // HTTP-клиент
    implementation (libs.gson) // JSON-парсинг

    implementation ("io.ktor:ktor-io:2.3.3")
    implementation ("com.supabase:postgrest-kt:1.2.0")
    implementation ("io.ktor:ktor-client-core:2.3.3")
    implementation ("io.ktor:ktor-client-okhttp:2.3.3")
    implementation ("io.ktor:ktor-client-content-negotiation:2.3.3")
    implementation ("io.ktor:ktor-serialization-gson:2.3.3")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation ("com.google.code.gson:gson:2.10.1")


    implementation (libs.material.v190)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}