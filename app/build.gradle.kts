plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.passwordgenerator"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.passwordgenerator"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)         // уже есть Material — можно оставить
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // ↓ добавьте новые строки с корректным Kotlin-DSL
    implementation("com.google.android.material:material:1.11.0")
    implementation("com.nulab-inc:zxcvbn:1.5.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}