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
        versionName = "0.00000001"

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



    implementation (libs.gson) // JSON-парсинг


    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
// Добавьте зависимость для Supabase Auth
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.2.3")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.2.3")


    implementation ("com.google.android.material:material:1.11.0")

    implementation (libs.material.v190)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}