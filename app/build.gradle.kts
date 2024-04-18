plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.runner"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.runner"
        minSdk = 24
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //material design
    implementation("com.google.android.material:material:1.11.0")

    //architectural components
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    //roomdb
    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    //coroutines support for room
    implementation("androidx.room:room-ktx:2.6.1")

    //coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    //coroutines lifecycle scope
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    //navigation components
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    //glide
    implementation("com.github.bumptech.glide:glide:4.15.0")
    ksp("com.github.bumptech.glide:compiler:4.15.0")

    //gmap services
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    //dagger core
    implementation("com.google.dagger:dagger:2.28.3")
    ksp("com.google.dagger:dagger-compiler:2.25.2")


    //dagger android
    api("com.google.dagger:dagger-android:2.28.1")
    api("com.google.dagger:dagger-android-support:2.28.1")
    ksp("com.google.dagger:dagger-android-processor:2.23.2")

    //easy permissions
    implementation("pub.devrel:easypermissions:3.0.0")

    //timber
    implementation("com.jakewharton.timber:timber:4.7.1")

    //charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("android.arch.lifecycle:extensions:1.1.1")

}