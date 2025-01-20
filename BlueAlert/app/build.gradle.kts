plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    namespace = "com.example.mqttapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mqttapp"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.org.eclipse.paho.client.mqttv3)
    implementation(libs.org.eclipse.paho.android.service)
    implementation(libs.androidx.core.ktx.v1100)
    implementation(libs.androidx.appcompat.v161)
    implementation("com.google.android.material:material:1.11.0")
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.constraintlayout.v214)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation(libs.androidx.espresso.core.v351)
    implementation(libs.billing)
    implementation(libs.app.update)
    implementation(libs.app.update.ktx)
    implementation(libs.graphview)
    implementation("com.github.anastr:speedviewlib:1.6.1")
}
