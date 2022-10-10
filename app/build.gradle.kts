@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("kotlin-android")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("org.jetbrains.kotlin.kapt")
    id("kotlin-noarg")
}


android {
    compileSdk = 33
    buildToolsVersion = "33.0.0"

    defaultConfig {
        resourceConfigurations.add("ru")
        applicationId = "gcu.product.mcard"
        minSdk = 21
        targetSdk = 33
        versionCode = 3
        versionName = "Rainfall 3.4.4"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    noArg {
        annotation("com.example.mcard.Repository.Features.Optionally.EmptyConstructor")
        invokeInitializers = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf("-Xjvm-default=all-compatibility")
    }


    buildFeatures {
        viewBinding = true
    }

    namespace = "com.example.mcard"
}

repositories {
    google()
    mavenCentral()
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.2")
    implementation("com.google.android.gms:play-services-location:20.0.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")
    implementation("androidx.paging:paging-runtime-ktx:3.2.0-alpha02")
    implementation("com.google.android.material:material:1.6.1")

    implementation("com.google.android.gms:play-services-vision:20.1.3")
    implementation("com.google.zxing:core:3.5.0")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0@aar")

    implementation("com.google.firebase:firebase-core:21.1.1")
    implementation("com.google.firebase:firebase-auth:21.0.8")
    implementation("com.google.firebase:firebase-database:20.0.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")

    implementation("ru.yoomoney.sdk.kassa.payments:yookassa-android-sdk:6.5.3")
    implementation("com.yandex.android:mobmetricalib:5.2.0")
    implementation("com.yandex.android:mobmetricapushlib:2.2.0") {
        exclude(group = ("com.squareup.okhttp3"), module = "okhttp")
    }

    implementation("com.google.firebase:firebase-messaging:23.0.8")
    implementation("com.google.firebase:firebase-firestore:24.3.1")
    implementation("com.google.android.gms:play-services-base:18.1.0")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("com.google.firebase:firebase-storage-ktx:20.0.2")
    platform("com.google.firebase:firebase-bom:30.5.0")
    implementation("com.google.firebase:firebase-database-ktx:20.0.6")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.20")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("com.github.kirich1409:viewbindingpropertydelegate:1.5.6")

    @Suppress("LifecycleAnnotationProcessorWithJava8")
    kapt("androidx.lifecycle:lifecycle-compiler:2.5.1")
    kapt("com.google.dagger:dagger-compiler:2.44")
    kapt("androidx.room:room-compiler:2.4.3")

    implementation("me.jfenn.ColorPickerDialog:base:0.2.2")
    implementation("com.jaredrummler:animated-svg-view:1.0.6")
    implementation("com.mig35:carousellayoutmanager:1.4.6")
    implementation("com.github.amyu:StackCardLayoutManager:1.0")
    implementation("jp.wasabeef:blurry:4.0.1")
    implementation("io.coil-kt:coil:2.2.2")

    implementation("com.google.dagger:dagger:2.44")
    implementation("androidx.room:room-runtime:2.4.3")
    implementation("androidx.room:room-ktx:2.4.3")
}

apply(plugin = "com.google.gms.google-services")
apply(plugin = "androidx.navigation.safeargs.kotlin")

