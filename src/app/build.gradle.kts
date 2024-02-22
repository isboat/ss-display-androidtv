plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.onscreensync.tvapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.onscreensync.tvapp"
        minSdk = 21
        targetSdk = 33
        versionCode = 2
        versionName = "2.0"

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildToolsVersion = "34.0.0"
}

dependencies {

    implementation("com.squareup.picasso:picasso:2.8")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")
    implementation("androidx.leanback:leanback:1.0.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.work:work-runtime:2.9.0")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.microsoft.signalr:signalr:7.0.0")
}